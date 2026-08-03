package com.uko.eaas.payment.service.impl;

import com.uko.eaas.payment.client.EscrowServiceClient;
import com.uko.eaas.payment.client.InterswitchClient;
import com.uko.eaas.payment.dto.*;
import com.uko.eaas.payment.messaging.event.PaymentEvent;
import com.uko.eaas.payment.model.entity.PaymentTransaction;
import com.uko.eaas.payment.model.enums.EscrowStatus;
import com.uko.eaas.payment.model.enums.PaymentChannel;
import com.uko.eaas.payment.model.enums.PaymentStatus;
import com.uko.eaas.payment.repository.PaymentTransactionRepository;
import com.uko.eaas.payment.service.PaymentService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentTransactionRepository paymentRepository;
    private final InterswitchClient interswitchClient;
    private final EscrowServiceClient escrowServiceClient;
    private final RabbitTemplate rabbitTemplate;
    private final com.uko.eaas.payment.service.AuditPublisher auditPublisher;
    private final com.uko.eaas.payment.service.AmlMonitoringService amlMonitoringService;

    @Override
    public InitializePaymentResponse initializePayment(InitializePaymentRequest request) {
        log.info("Initializing payment for escrow: {}", request.getEscrowReference());

        // Fetch escrow details to get actual amounts and IDs
        EscrowData escrow = escrowServiceClient.getEscrow(request.getEscrowReference());

        //Validate that an escrow has a status of INITIATED
        if (escrow.getStatus() != EscrowStatus.INITIATED) {
            throw new IllegalArgumentException("Cannot initialize payment for escrow with status: " + escrow.getStatus());
        }

        // Validate that the payment request amount matches the escrow amount
        if (request.getAmount().compareTo(escrow.getAmount()) != 0) {
            throw new IllegalArgumentException(
                    "Amount mismatch: request amount " + request.getAmount() +
                    " does not match escrow amount " + escrow.getAmount());
        }

        // Idempotency check
        if (request.getIdempotencyKey() != null) {
            Optional<PaymentTransaction> existing = paymentRepository.findByIdempotencyKey(request.getIdempotencyKey());
            if (existing.isPresent()) {
                log.info("Returning existing payment for idempotency key: {}", request.getIdempotencyKey());
                PaymentTransaction tx = existing.get();
                return InitializePaymentResponse.builder()
                        .reference(tx.getReference())
                        .escrowReference(tx.getEscrowReference())
                        .paymentLink(tx.getPaymentLink())
                        .amount(tx.getAmount())
                        .currency(tx.getCurrency())
                        .status(tx.getStatus().toString())
                        .replayed(true)
                        .build();
            }
        }

        // Generate reference
        String reference = generateReference();

        // Call Interswitch to initialize
        InitializePaymentResponse response = interswitchClient.initializePayment(request, reference);

        // Save payment record with data from the escrow
        PaymentTransaction payment = PaymentTransaction.builder()
                .reference(reference)
                .escrowReference(request.getEscrowReference())
                .customerId(escrow.getCustomerId())
                .merchantId(escrow.getMerchantId())
                .amount(escrow.getAmount())
                .fee(escrow.getEscrowFee())
                .currency(request.getCurrency())
                .status(PaymentStatus.PENDING)
                .paymentLink(response.getPaymentLink())
                .idempotencyKey(request.getIdempotencyKey())
                .build();

        paymentRepository.save(payment);

        auditPublisher.publish(com.uko.eaas.payment.messaging.event.AuditEvent.builder()
                .eventType("PAYMENT_INITIALIZED")
                .entityType("PAYMENT")
                .entityId(reference)
                .action("CREATE")
                .metadata("{\"escrowReference\": \"" + request.getEscrowReference() + "\", \"amount\": " + escrow.getAmount() + ", \"fee\": " + escrow.getEscrowFee() + "}")
                .build());

        log.info("Payment initialized with reference: {} for escrow: {} amount: {} fee: {}",
                reference, request.getEscrowReference(), escrow.getAmount(), escrow.getEscrowFee());
        return response;
    }

    @Override
    public PaymentVerificationResponse verifyPayment(String reference) {
        log.info("Verifying payment: {}", reference);

        PaymentTransaction payment = paymentRepository.findByReference(reference)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found: " + reference));

        if (payment.getInterswitchRef() == null) {
            throw new IllegalStateException("Payment not yet submitted to Interswitch");
        }

        PaymentVerificationResponse verification = interswitchClient.verifyPayment(
                payment.getInterswitchRef(), payment.getAmount());

        if (verification != null) {
            // Update payment status
            PaymentStatus newStatus = mapInterswitchStatus(verification.getStatus());
            if (payment.getStatus() != newStatus) {
                payment.setStatus(newStatus);
                payment.setPaidAt(verification.getPaidAt());
                payment.setChannel(mapChannel(verification.getChannel()));
                payment.setCardLast4(verification.getCardLast4());
                payment.setCardBrand(verification.getCardBrand());
                payment.setBankName(verification.getBankName());

                paymentRepository.save(payment);

                // Publish event
                publishPaymentEvent(payment);
            }
        }

        return verification;
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentVerificationResponse getPayment(String reference) {
        PaymentTransaction payment = paymentRepository.findByReference(reference)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found: " + reference));

        return mapToVerificationResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentVerificationResponse> listPayments(String userId, String role, Pageable pageable) {
        UUID userUuid = UUID.fromString(userId);

        Page<PaymentTransaction> payments;
        if ("MERCHANT".equals(role)) {
            payments = paymentRepository.findByMerchantId(userUuid, pageable);
        } else {
            payments = paymentRepository.findByCustomerId(userUuid, pageable);
        }

        return payments.map(this::mapToVerificationResponse);
    }

    @Override
    public void handleWebhook(PaymentWebhookPayload payload, String signature) {
        log.info("Processing payment webhook for reference: {}", payload.getReference());

        // Verify signature
        String payloadJson = convertToJson(payload);
        if (!interswitchClient.verifyWebhookSignature(payloadJson, signature)) {
            log.error("Invalid webhook signature");
            throw new SecurityException("Invalid webhook signature");
        }

        PaymentTransaction payment = paymentRepository.findByReference(payload.getReference())
                .orElseThrow(() -> new EntityNotFoundException("Payment not found: " + payload.getReference()));

        // Update payment
        PaymentStatus newStatus = mapInterswitchStatus(payload.getStatus());
        payment.setStatus(newStatus);
        payment.setInterswitchRef(payload.getInterswitchRef());
        payment.setPaidAt(payload.getPaidAt());
        payment.setChannel(mapChannel(payload.getChannel()));
        payment.setCardLast4(payload.getCardLast4());
        payment.setCardBrand(payload.getCardBrand());
        payment.setBankName(payload.getBankName());

        if ("SUCCESS".equalsIgnoreCase(payload.getStatus()) && payment.getFee() == null) {
            payment.setFee(BigDecimal.ZERO);
        }

        paymentRepository.save(payment);

        // Publish event
        publishPaymentEvent(payment);

        // AML monitoring
        amlMonitoringService.evaluateTransaction(payment);

        log.info("Payment {} updated via webhook to status: {}", payload.getReference(), newStatus);
    }

    @Override
    public void processRefund(String reference, String reason) {
        log.info("Processing refund for payment: {}", reference);

        PaymentTransaction payment = paymentRepository.findByReference(reference)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found: " + reference));

        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new IllegalStateException("Can only refund successful payments");
        }

        // Initiate refund via Interswitch
        String refundRef = interswitchClient.initiateRefund(payment.getReference(), payment.getAmount());

        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setRefundedAt(LocalDateTime.now());
        payment.setRefundAmount(payment.getAmount());
        payment.setRefundReason(reason);
        payment.setInterswitchRefundRef(refundRef);

        paymentRepository.save(payment);

        // Publish refund event
        publishRefundEvent(payment);

        auditPublisher.publish(com.uko.eaas.payment.messaging.event.AuditEvent.builder()
                .eventType("PAYMENT_REFUNDED")
                .entityType("PAYMENT")
                .entityId(reference)
                .action("REFUND")
                .metadata("{\"reason\": \"" + reason + "\", \"refundAmount\": " + payment.getRefundAmount()
                        + ", \"interswitchRefundRef\": \"" + refundRef + "\"}")
                .build());

        log.info("Payment {} marked as refunded (Interswitch ref: {})", reference, refundRef);
    }

    private String generateReference() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String random = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "PAY-" + timestamp + "-" + random;
    }

    private PaymentStatus mapInterswitchStatus(String status) {
        return switch (status.toUpperCase()) {
            case "SUCCESS" -> PaymentStatus.SUCCESS;
            case "FAILED" -> PaymentStatus.FAILED;
            case "PENDING" -> PaymentStatus.PENDING;
            case "PROCESSING" -> PaymentStatus.PROCESSING;
            default -> PaymentStatus.FAILED;
        };
    }

    private PaymentChannel mapChannel(String channel) {
        if (channel == null) return null;
        return switch (channel.toUpperCase()) {
            case "CARD" -> PaymentChannel.CARD;
            case "BANK_TRANSFER", "BANK" -> PaymentChannel.BANK_TRANSFER;
            case "USSD" -> PaymentChannel.USSD;
            case "QR" -> PaymentChannel.QR;
            case "MOBILE_MONEY" -> PaymentChannel.MOBILE_MONEY;
            default -> null;
        };
    }

    private PaymentVerificationResponse mapToVerificationResponse(PaymentTransaction payment) {
        return PaymentVerificationResponse.builder()
                .reference(payment.getReference())
                .status(payment.getStatus().toString())
                .amount(payment.getAmount())
                .channel(payment.getChannel() != null ? payment.getChannel().toString() : null)
                .cardLast4(payment.getCardLast4())
                .cardBrand(payment.getCardBrand())
                .bankName(payment.getBankName())
                .paidAt(payment.getPaidAt())
                .build();
    }

    private void publishPaymentEvent(PaymentTransaction payment) {
        PaymentEvent event = PaymentEvent.builder()
                .eventType("payment." + payment.getStatus().toString().toLowerCase())
                .reference(payment.getReference())
                .escrowReference(payment.getEscrowReference())
                .customerId(payment.getCustomerId() != null ? payment.getCustomerId().toString() : null)
                .merchantId(payment.getMerchantId() != null ? payment.getMerchantId().toString() : null)
                .amount(payment.getAmount())
                .fee(payment.getFee())
                .currency(payment.getCurrency())
                .status(payment.getStatus().toString())
                .channel(payment.getChannel() != null ? payment.getChannel().toString() : null)
                .cardLast4(payment.getCardLast4())
                .cardBrand(payment.getCardBrand())
                .bankName(payment.getBankName())
                .timestamp(LocalDateTime.now())
                .build();

        rabbitTemplate.convertAndSend("eaas.exchange", "payment." + payment.getStatus().toString().toLowerCase(), event);
    }

    private void publishRefundEvent(PaymentTransaction payment) {
        PaymentEvent event = PaymentEvent.builder()
                .eventType("payment.refunded")
                .reference(payment.getReference())
                .escrowReference(payment.getEscrowReference())
                .customerId(payment.getCustomerId() != null ? payment.getCustomerId().toString() : null)
                .merchantId(payment.getMerchantId() != null ? payment.getMerchantId().toString() : null)
                .amount(payment.getAmount())
                .refundAmount(payment.getRefundAmount())
                .refundReason(payment.getRefundReason())
                .currency(payment.getCurrency())
                .timestamp(LocalDateTime.now())
                .build();

        rabbitTemplate.convertAndSend("eaas.exchange", "payment.refunded", event);
    }

    private String convertToJson(PaymentWebhookPayload payload) {
        // Simple JSON conversion - in production use ObjectMapper
        return "{\"event\":\"" + payload.getEvent() + "\",\"reference\":\"" + payload.getReference() + "\"}";
    }
}
