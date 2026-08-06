package com.uko.eaas.escrow.service.impl;

import com.uko.eaas.escrow.dto.*;
import com.uko.eaas.escrow.messaging.event.EscrowEvent;
import com.uko.eaas.escrow.model.entity.EscrowStateHistory;
import com.uko.eaas.escrow.model.entity.EscrowTransaction;
import com.uko.eaas.escrow.model.enums.EscrowStatus;
import com.uko.eaas.escrow.model.enums.TriggeredBy;
import com.uko.eaas.escrow.repository.EscrowStateHistoryRepository;
import com.uko.eaas.escrow.repository.EscrowTransactionRepository;
import com.uko.eaas.escrow.service.EscrowService;
import com.uko.eaas.escrow.service.FeeCalculationService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class EscrowServiceImpl implements EscrowService {

    private final EscrowTransactionRepository escrowRepository;
    private final EscrowStateHistoryRepository stateHistoryRepository;
    private final FeeCalculationService feeCalculationService;
    private final RabbitTemplate rabbitTemplate;

    @Value("${escrow.confirmation.window-hours:72}")
    private int confirmationWindowHours;

    @Value("${escrow.payment.expiry-hours:24}")
    private int paymentExpiryHours;

    // State machine transition rules: currentStatus -> allowed next statuses
    private static final Map<EscrowStatus, List<EscrowStatus>> STATE_TRANSITIONS = new EnumMap<>(EscrowStatus.class);

    static {
        STATE_TRANSITIONS.put(EscrowStatus.INITIATED, Arrays.asList(EscrowStatus.FUNDED, EscrowStatus.CANCELLED));
        STATE_TRANSITIONS.put(EscrowStatus.FUNDED, Arrays.asList(EscrowStatus.MERCHANT_NOTIFIED, EscrowStatus.CANCELLED));
        STATE_TRANSITIONS.put(EscrowStatus.MERCHANT_NOTIFIED, Arrays.asList(EscrowStatus.SHIPPED, EscrowStatus.CANCELLED));
        STATE_TRANSITIONS.put(EscrowStatus.SHIPPED, Arrays.asList(EscrowStatus.DELIVERED, EscrowStatus.DISPUTED));
        STATE_TRANSITIONS.put(EscrowStatus.DELIVERED, Arrays.asList(EscrowStatus.CONFIRMED, EscrowStatus.DISPUTED, EscrowStatus.AUTO_RELEASED));
        STATE_TRANSITIONS.put(EscrowStatus.CONFIRMED, List.of(EscrowStatus.RELEASED));
        STATE_TRANSITIONS.put(EscrowStatus.AUTO_RELEASED, List.of(EscrowStatus.RELEASED));
        STATE_TRANSITIONS.put(EscrowStatus.DISPUTED, Arrays.asList(EscrowStatus.UNDER_REVIEW, EscrowStatus.RESOLVED_MERCHANT, EscrowStatus.RESOLVED_CUSTOMER));
        STATE_TRANSITIONS.put(EscrowStatus.UNDER_REVIEW, Arrays.asList(EscrowStatus.RESOLVED_MERCHANT, EscrowStatus.RESOLVED_CUSTOMER));
        STATE_TRANSITIONS.put(EscrowStatus.RESOLVED_MERCHANT, List.of(EscrowStatus.RELEASED));
        STATE_TRANSITIONS.put(EscrowStatus.RESOLVED_CUSTOMER, List.of(EscrowStatus.REFUNDED));
    }

    @Override
    public EscrowResponse createEscrow(CreateEscrowRequest request, String customerId, String idempotencyKey) {
        log.info("Creating escrow for customer: {}, merchant: {}", customerId, request.getMerchantId());

        // Idempotency check - using header value, not request body
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            Optional<EscrowTransaction> existing = escrowRepository.findByIdempotencyKey(idempotencyKey);
            if (existing.isPresent()) {
                log.info("Returning existing escrow for idempotency key: {}", idempotencyKey);
                return mapToResponse(existing.get());
            }
        }

        // Calculate fees
        FeeBreakdown fees = feeCalculationService.calculateFee(
                request.getAmount(), UUID.fromString(request.getMerchantId()));

        // Generate reference
        String reference = generateReference();

        // Calculate expiry time
        LocalDateTime paymentExpiresAt = LocalDateTime.now().plusHours(paymentExpiryHours);

        EscrowTransaction escrow = EscrowTransaction.builder()
                .reference(reference)
                .customerId(UUID.fromString(customerId))
                .merchantId(UUID.fromString(request.getMerchantId()))
                .merchantProfileId(UUID.fromString(request.getMerchantId())) // Same as merchantId for now
                .amount(request.getAmount())
                .escrowFee(fees.getEscrowFee())
                .merchantAmount(fees.getMerchantAmount())
                .currency("NGN")
                .status(EscrowStatus.INITIATED)
                .productDescription(request.getProductDescription())
                .productQuantity(request.getProductQuantity())
                .agreedDeliveryDays(request.getAgreedDeliveryDays())
                .paymentExpiresAt(paymentExpiresAt)
                .idempotencyKey(idempotencyKey)
                .build();

        escrow = escrowRepository.save(escrow);

        // Record initial state
        recordStateTransition(escrow, null, EscrowStatus.INITIATED, TriggeredBy.CUSTOMER, UUID.fromString(customerId),
                "Escrow transaction created");

        // Publish event
        publishEvent("escrow.created", escrow, "CUSTOMER");

        log.info("Escrow created with reference: {}", reference);
        return mapToResponse(escrow);
    }

    @Override
    @Transactional(readOnly = true)
    public EscrowResponse getEscrow(String reference) {
        EscrowTransaction escrow = escrowRepository.findByReference(reference)
                .orElseThrow(() -> new EntityNotFoundException("Escrow not found: " + reference));
        return mapToResponse(escrow);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EscrowResponse> listAllEscrows(String userId, String role, Pageable pageable) {
        UUID userUuid = UUID.fromString(userId);

        Page<EscrowTransaction> escrows;
        if ("MERCHANT".equals(role)) {
            escrows = escrowRepository.findByMerchantId(userUuid, pageable);
        } else {
            escrows = escrowRepository.findByCustomerId(userUuid, pageable);
        }

        return escrows.map(this::mapToResponse);
    }

    @Override
    public EscrowResponse shipEscrow(String reference, ShipEscrowRequest request) {
        log.info("Shipping escrow: {}", reference);

        EscrowTransaction escrow = escrowRepository.findByReference(reference)
                .orElseThrow(() -> new EntityNotFoundException("Escrow not found: " + reference));

        validateStateTransition(escrow.getStatus(), EscrowStatus.SHIPPED);

        EscrowStatus oldStatus = escrow.getStatus();

        escrow.setTrackingNumber(request.getTrackingNumber());
        escrow.setLogisticsProvider(request.getLogisticsProvider());
        escrow.setEstimatedDeliveryDate(LocalDate.from(request.getEstimatedDeliveryDate()));
        escrow.setShippedAt(LocalDateTime.now());
        escrow.setStatus(EscrowStatus.SHIPPED);

        escrow = escrowRepository.save(escrow);

        recordStateTransition(escrow, oldStatus, EscrowStatus.SHIPPED, TriggeredBy.MERCHANT, escrow.getMerchantId(),
                "Order shipped via " + request.getLogisticsProvider());

        // Notify customer
        publishEvent("escrow.shipped", escrow, "MERCHANT");

        log.info("Escrow {} marked as shipped", reference);
        return mapToResponse(escrow);
    }

    @Override
    public EscrowResponse deliverEscrow(String reference, String customerId) {
        log.info("Marking escrow as delivered: {}", reference);

        EscrowTransaction escrow = escrowRepository.findByReference(reference)
                .orElseThrow(() -> new EntityNotFoundException("Escrow not found: " + reference));

        // Verify customer owns this escrow
        if (!escrow.getCustomerId().equals(UUID.fromString(customerId))) {
            throw new IllegalStateException("Only the customer can mark the escrow as delivered");
        }

        validateStateTransition(escrow.getStatus(), EscrowStatus.DELIVERED);

        EscrowStatus oldStatus = escrow.getStatus();

        // Calculate confirmation deadline
        LocalDateTime confirmationDeadline = LocalDateTime.now().plusHours(confirmationWindowHours);

        escrow.setDeliveredAt(LocalDateTime.now());
        escrow.setConfirmationDeadline(confirmationDeadline);
        escrow.setAutoReleaseAt(confirmationDeadline);
        escrow.setStatus(EscrowStatus.DELIVERED);

        escrow = escrowRepository.save(escrow);

        recordStateTransition(escrow, oldStatus, EscrowStatus.DELIVERED, TriggeredBy.SYSTEM, null,
                "Order delivered. Customer has " + confirmationWindowHours + " hours to confirm or dispute.");

        // Notify customer
        publishEvent("escrow.delivered", escrow, "SYSTEM");

        log.info("Escrow {} marked as delivered, confirmation deadline: {}", reference, confirmationDeadline);
        return mapToResponse(escrow);
    }

    @Override
    public EscrowResponse confirmEscrow(String reference, String customerId) {
        log.info("Customer {} confirming escrow: {}", customerId, reference);

        EscrowTransaction escrow = escrowRepository.findByReference(reference)
                .orElseThrow(() -> new EntityNotFoundException("Escrow not found: " + reference));

        // Verify customer owns this escrow
        if (!escrow.getCustomerId().equals(UUID.fromString(customerId))) {
            throw new IllegalStateException("Only the customer can confirm delivery");
        }

        validateStateTransition(escrow.getStatus(), EscrowStatus.CONFIRMED);

        EscrowStatus oldStatus = escrow.getStatus();

        escrow.setConfirmedAt(LocalDateTime.now());
        escrow.setStatus(EscrowStatus.CONFIRMED);

        escrow = escrowRepository.save(escrow);

        recordStateTransition(escrow, oldStatus, EscrowStatus.CONFIRMED, TriggeredBy.CUSTOMER, UUID.fromString(customerId),
                "Customer confirmed delivery");

        // Trigger payout
        publishEvent("escrow.confirmed", escrow, "CUSTOMER");

        log.info("Escrow {} confirmed by customer", reference);
        return mapToResponse(escrow);
    }

    @Override
    public void cancelEscrow(String reference, String userId, String role) {
        log.info("Cancelling escrow: {} by user: {} (role: {})", reference, userId, role);

        EscrowTransaction escrow = escrowRepository.findByReference(reference)
                .orElseThrow(() -> new EntityNotFoundException("Escrow not found: " + reference));

        UUID userUuid = UUID.fromString(userId);
        boolean isAdmin = "ADMIN".equals(role);
        boolean isCustomer = escrow.getCustomerId().equals(userUuid);

        if (!isAdmin && !isCustomer) {
            throw new IllegalStateException("Only the customer or an admin can cancel this escrow");
        }

        // Only allow cancellation in certain states
        if (!Arrays.asList(EscrowStatus.INITIATED, EscrowStatus.FUNDED, EscrowStatus.MERCHANT_NOTIFIED).contains(escrow.getStatus())) {
            throw new IllegalStateException("Cannot cancel escrow in status: " + escrow.getStatus());
        }

        EscrowStatus oldStatus = escrow.getStatus();

        escrow.setStatus(EscrowStatus.CANCELLED);
        escrow = escrowRepository.save(escrow);

        recordStateTransition(escrow, oldStatus, EscrowStatus.CANCELLED,
                isAdmin ? TriggeredBy.ADMIN : TriggeredBy.CUSTOMER, userUuid,
                "Escrow cancelled by " + (isAdmin ? "admin" : "customer"));

        // If funded, trigger refund
        if (oldStatus == EscrowStatus.FUNDED || oldStatus == EscrowStatus.MERCHANT_NOTIFIED) {
            publishEvent("escrow.cancelled.refund", escrow, role);
        }

        log.info("Escrow {} cancelled by user: {}", reference, userId);
    }

    @Override
    public EscrowResponse raiseDispute(String reference) {
        log.info("Raising dispute for escrow: {}", reference);

        EscrowTransaction escrow = escrowRepository.findByReference(reference)
                .orElseThrow(() -> new EntityNotFoundException("Escrow not found: " + reference));

        validateStateTransition(escrow.getStatus(), EscrowStatus.DISPUTED);

        EscrowStatus oldStatus = escrow.getStatus();

        escrow.setStatus(EscrowStatus.DISPUTED);
        escrow = escrowRepository.save(escrow);

        recordStateTransition(escrow, oldStatus, EscrowStatus.DISPUTED, TriggeredBy.CUSTOMER, escrow.getCustomerId(),
                "Dispute raised by customer");

        publishEvent("escrow.disputed", escrow, "CUSTOMER");

        log.info("Escrow {} moved to DISPUTED", reference);
        return mapToResponse(escrow);
    }

    @Override
    public EscrowResponse resolveDispute(String reference, String resolution) {
        log.info("Resolving dispute for escrow: {} with resolution: {}", reference, resolution);

        EscrowTransaction escrow = escrowRepository.findByReference(reference)
                .orElseThrow(() -> new EntityNotFoundException("Escrow not found: " + reference));

        if (escrow.getStatus() != EscrowStatus.DISPUTED) {
            throw new IllegalStateException("Cannot resolve escrow in status: " + escrow.getStatus());
        }

        EscrowStatus oldStatus = escrow.getStatus();

        switch (resolution) {
            case "RESOLVED_MERCHANT" -> {
                escrow.setStatus(EscrowStatus.RESOLVED_MERCHANT);
                escrow = escrowRepository.save(escrow);
                recordStateTransition(escrow, oldStatus, EscrowStatus.RESOLVED_MERCHANT, TriggeredBy.ADMIN, null,
                        "Dispute resolved in merchant's favor");

                escrow.setStatus(EscrowStatus.RELEASED);
                escrow = escrowRepository.save(escrow);
                recordStateTransition(escrow, EscrowStatus.RESOLVED_MERCHANT, EscrowStatus.RELEASED, TriggeredBy.SYSTEM, null,
                        "Funds released to merchant");

                publishEvent("escrow.resolved-merchant", escrow, "ADMIN");
            }
            case "RESOLVED_CUSTOMER" -> {
                escrow.setStatus(EscrowStatus.RESOLVED_CUSTOMER);
                escrow = escrowRepository.save(escrow);
                recordStateTransition(escrow, oldStatus, EscrowStatus.RESOLVED_CUSTOMER, TriggeredBy.ADMIN, null,
                        "Dispute resolved in customer's favor");

                escrow.setStatus(EscrowStatus.REFUNDED);
                escrow = escrowRepository.save(escrow);
                recordStateTransition(escrow, EscrowStatus.RESOLVED_CUSTOMER, EscrowStatus.REFUNDED, TriggeredBy.SYSTEM, null,
                        "Funds refunded to customer");

                publishEvent("escrow.resolved-customer", escrow, "ADMIN");
            }
            default -> throw new IllegalArgumentException("Unsupported dispute resolution: " + resolution);
        }

        log.info("Escrow {} resolved with resolution: {}", reference, resolution);
        return mapToResponse(escrow);
    }

    @Override
    public void handlePaymentWebhook(PaymentWebhookRequest request) {
        log.info("Processing payment webhook for reference: {}", request.getReference());

        EscrowTransaction escrow = escrowRepository.findByReference(request.getReference())
                .orElseThrow(() -> new EntityNotFoundException("Escrow not found: " + request.getReference()));

        if (escrow.getStatus() == EscrowStatus.FUNDED || escrow.getStatus() == EscrowStatus.MERCHANT_NOTIFIED) {
            log.info("Escrow {} already funded (status: {}), ignoring duplicate payment event",
                    request.getReference(), escrow.getStatus());
            return;
        }

        if (!"success".equalsIgnoreCase(request.getStatus())) {
            log.warn("Payment failed for escrow: {}", request.getReference());
            return;
        }

        // Validate amounts match
        if (escrow.getAmount().compareTo(request.getAmount()) != 0) {
            log.error("Amount mismatch for escrow {}: expected {}, got {}",
                    request.getReference(), escrow.getAmount(), request.getAmount());
            throw new IllegalStateException("Payment amount mismatch");
        }

        EscrowStatus oldStatus = escrow.getStatus();

        escrow.setPaymentReference(request.getPaymentReference());
        escrow.setPaymentChannel(request.getChannel());
        escrow.setFundedAt(request.getPaidAt());
        escrow.setStatus(EscrowStatus.FUNDED);

        escrow = escrowRepository.save(escrow);

        recordStateTransition(escrow, oldStatus, EscrowStatus.FUNDED, TriggeredBy.SYSTEM, null,
                "Payment received via " + request.getChannel());

        // Auto-advance to MERCHANT_NOTIFIED (the escrow.funded event is the merchant notification)
        escrow.setStatus(EscrowStatus.MERCHANT_NOTIFIED);
        escrow = escrowRepository.save(escrow);

        recordStateTransition(escrow, EscrowStatus.FUNDED, EscrowStatus.MERCHANT_NOTIFIED, TriggeredBy.SYSTEM, null,
                "Merchant notified of funding");

        // Notify merchant
        publishEvent("escrow.funded", escrow, "SYSTEM");

        log.info("Escrow {} marked as funded and merchant notified", request.getReference());
    }

    @Override
    @Transactional
    public void expireUnfundedEscrows() {
        log.debug("Checking for expired unfunded escrows");

        List<EscrowTransaction> expired = escrowRepository.findExpiredForCancellation(LocalDateTime.now());

        for (EscrowTransaction escrow : expired) {
            try {
                EscrowStatus oldStatus = escrow.getStatus();
                escrow.setStatus(EscrowStatus.CANCELLED);
                escrowRepository.save(escrow);

                recordStateTransition(escrow, oldStatus, EscrowStatus.CANCELLED, TriggeredBy.SYSTEM, null,
                        "Payment window expired");

                publishEvent("escrow.expired", escrow, "SYSTEM");

                log.info("Expired unfunded escrow: {}", escrow.getReference());
            } catch (Exception e) {
                log.error("Failed to expire escrow {}: {}", escrow.getReference(), e.getMessage());
            }
        }
    }

    @Override
    @Transactional
    public void autoReleaseEscrows() {
        log.debug("Checking for auto-release escrows");

        List<EscrowTransaction> expired = escrowRepository.findExpiredForAutoRelease(
                EscrowStatus.DELIVERED, LocalDateTime.now());

        for (EscrowTransaction escrow : expired) {
            try {
                EscrowStatus oldStatus = escrow.getStatus();
                escrow.setStatus(EscrowStatus.AUTO_RELEASED);
                escrowRepository.save(escrow);

                recordStateTransition(escrow, oldStatus, EscrowStatus.AUTO_RELEASED, TriggeredBy.SYSTEM, null,
                        "Auto-released after " + confirmationWindowHours + " hours without customer action");

                // Auto-confirm and trigger payout
                escrow.setStatus(EscrowStatus.CONFIRMED);
                escrow.setConfirmedAt(LocalDateTime.now());
                escrow = escrowRepository.save(escrow);

                recordStateTransition(escrow, EscrowStatus.AUTO_RELEASED, EscrowStatus.CONFIRMED, TriggeredBy.SYSTEM, null,
                        "Auto-confirmed for payout");

                publishEvent("escrow.auto-released", escrow, "SYSTEM");

                log.info("Auto-released escrow: {}", escrow.getReference());
            } catch (Exception e) {
                log.error("Failed to auto-release escrow {}: {}", escrow.getReference(), e.getMessage());
            }
        }
    }

    private void validateStateTransition(EscrowStatus currentStatus, EscrowStatus newStatus) {

        if (currentStatus == newStatus) {
            throw new IllegalStateException("Escrow is already in the specified status: " + currentStatus);
        }
        List<EscrowStatus> allowedTransitions = STATE_TRANSITIONS.get(currentStatus);

        if (allowedTransitions == null || !allowedTransitions.contains(newStatus)) {
            throw new IllegalStateException(
                    String.format("Invalid state transition from %s to %s", currentStatus, newStatus));
        }
    }

    private void recordStateTransition(EscrowTransaction escrow, EscrowStatus fromStatus, EscrowStatus toStatus,
                                       TriggeredBy triggeredBy, UUID triggeredById, String reason) {
        EscrowStateHistory history = EscrowStateHistory.builder()
                .escrowId(escrow.getId())
                .fromStatus(fromStatus)
                .toStatus(toStatus)
                .triggeredBy(triggeredBy)
                .triggeredById(triggeredById)
                .reason(reason)
                .build();

        stateHistoryRepository.save(history);
    }

    private String generateReference() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String random = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "ESC-" + timestamp + "-" + random;
    }

    private void publishEvent(String routingKey, EscrowTransaction escrow, String triggeredBy) {
        try {
            EscrowEvent event = EscrowEvent.builder()
                    .eventType(routingKey)
                    .escrowId(escrow.getId().toString())
                    .reference(escrow.getReference())
                    .customerId(escrow.getCustomerId().toString())
                    .merchantId(escrow.getMerchantId().toString())
                    .status(escrow.getStatus().toString())
                    .amount(escrow.getAmount())
                    .merchantAmount(escrow.getMerchantAmount())
                    .escrowFee(escrow.getEscrowFee())
                    .currency(escrow.getCurrency())
                    .paymentReference(escrow.getPaymentReference())
                    .trackingNumber(escrow.getTrackingNumber())
                    .logisticsProvider(escrow.getLogisticsProvider())
                    .triggeredBy(triggeredBy)
                    .timestamp(LocalDateTime.now())
                    .build();

            rabbitTemplate.convertAndSend("eaas.exchange", routingKey, event);
            log.debug("Published event: {} for escrow: {}", routingKey, escrow.getReference());
        } catch (Exception e) {
            log.error("Failed to publish event {}: {}", routingKey, e.getMessage());
        }
    }

    private EscrowResponse mapToResponse(EscrowTransaction escrow) {
        return EscrowResponse.builder()
                .id(escrow.getId())
                .reference(escrow.getReference())
                .customerId(escrow.getCustomerId())
                .merchantId(escrow.getMerchantId())
                .amount(escrow.getAmount())
                .escrowFee(escrow.getEscrowFee())
                .merchantAmount(escrow.getMerchantAmount())
                .currency(escrow.getCurrency())
                .status(escrow.getStatus())
                .productDescription(escrow.getProductDescription())
                .productQuantity(escrow.getProductQuantity())
                .agreedDeliveryDays(escrow.getAgreedDeliveryDays())
                .paymentReference(escrow.getPaymentReference())
                .paymentChannel(escrow.getPaymentChannel())
                .paymentLink(escrow.getPaymentLink())
                .fundedAt(escrow.getFundedAt())
                .paymentExpiresAt(escrow.getPaymentExpiresAt())
                .trackingNumber(escrow.getTrackingNumber())
                .logisticsProvider(escrow.getLogisticsProvider())
                .estimatedDeliveryDate(escrow.getEstimatedDeliveryDate())
                .shippedAt(escrow.getShippedAt())
                .deliveredAt(escrow.getDeliveredAt())
                .confirmationDeadline(escrow.getConfirmationDeadline())
                .autoReleaseAt(escrow.getAutoReleaseAt())
                .confirmedAt(escrow.getConfirmedAt())
                .payoutReference(escrow.getPayoutReference())
                .paidOutAt(escrow.getPaidOutAt())
                .createdAt(escrow.getCreatedAt())
                .updatedAt(escrow.getUpdatedAt())
                .build();
    }
}
