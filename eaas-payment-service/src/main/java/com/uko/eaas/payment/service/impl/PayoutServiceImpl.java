package com.uko.eaas.payment.service.impl;

import com.uko.eaas.payment.client.InterswitchClient;
import com.uko.eaas.payment.dto.CreatePayoutRequest;
import com.uko.eaas.payment.dto.PayoutResponse;
import com.uko.eaas.payment.messaging.event.PayoutEvent;
import com.uko.eaas.payment.model.entity.Payout;
import com.uko.eaas.payment.model.enums.PayoutStatus;
import com.uko.eaas.payment.repository.PayoutRepository;
import com.uko.eaas.payment.service.PayoutService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PayoutServiceImpl implements PayoutService {

    private final PayoutRepository payoutRepository;
    private final InterswitchClient interswitchClient;
    private final RabbitTemplate rabbitTemplate;

    @Value("${payout.retry.attempts:6}")
    private int maxRetryAttempts;

    @Override
    public PayoutResponse createPayout(CreatePayoutRequest request) {
        log.info("Creating payout for escrow: {} to merchant: {}", request.getEscrowReference(), request.getMerchantId());

        // Check for duplicate
        if (payoutRepository.existsByEscrowReference(request.getEscrowReference())) {
            throw new IllegalStateException("Payout already exists for escrow: " + request.getEscrowReference());
        }

        // The escrow fee is already deducted at escrow creation (merchantAmount),
        // so the payout transfers the full requested amount with no additional fee.
        BigDecimal fee = BigDecimal.ZERO;
        BigDecimal netAmount = request.getAmount();

        String reference = generateReference();

        Payout payout = Payout.builder()
                .reference(reference)
                .escrowReference(request.getEscrowReference())
                .merchantId(UUID.fromString(request.getMerchantId()))
                .amount(request.getAmount())
                .fee(fee)
                .netAmount(netAmount)
                .currency("NGN")
                .status(PayoutStatus.PENDING)
                .method(request.getBankCode() != null ? com.uko.eaas.payment.model.enums.PayoutMethod.BANK_TRANSFER : null)
                .bankCode(request.getBankCode())
                .bankName(request.getBankName())
                .accountNumber(request.getAccountNumber())
                .accountName(request.getAccountName())
                .scheduledAt(request.getScheduledAt() != null ? request.getScheduledAt() : LocalDateTime.now())
                .retryCount(0)
                .build();

        payoutRepository.save(payout);

        log.info("Payout created with reference: {}", reference);
        return mapToResponse(payout);
    }

    @Override
    @Transactional(readOnly = true)
    public PayoutResponse getPayout(String reference) {
        Payout payout = payoutRepository.findByReference(reference)
                .orElseThrow(() -> new EntityNotFoundException("Payout not found: " + reference));
        return mapToResponse(payout);
    }

    @Override
    @Transactional(readOnly = true)
    public PayoutResponse getPayoutByEscrow(String escrowReference) {
        // Find by escrow reference - return first match
        return payoutRepository.findByEscrowReference(escrowReference)
                .map(this::mapToResponse)
                .orElseThrow(() -> new EntityNotFoundException("Payout not found for escrow: " + escrowReference));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PayoutResponse> listPayouts(UUID merchantId, Pageable pageable) {
        return payoutRepository.findByMerchantId(merchantId, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PayoutResponse> listAllPayouts(Pageable pageable) {
        return payoutRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Override
    @Transactional
    public PayoutResponse retryPayout(String reference) {
        log.info("Admin retrying payout: {}", reference);

        Payout payout = payoutRepository.findByReference(reference)
                .orElseThrow(() -> new EntityNotFoundException("Payout not found: " + reference));

        if (payout.getStatus() != PayoutStatus.FAILED && payout.getStatus() != PayoutStatus.PENDING) {
            throw new IllegalStateException("Cannot retry payout in status: " + payout.getStatus());
        }

        payout.setStatus(PayoutStatus.PENDING);
        payout.setRetryCount(0);
        payout.setNextRetryAt(null);
        payout.setFailureReason(null);
        payoutRepository.save(payout);

        processPayout(payout);

        return mapToResponse(payout);
    }

    @Override
    @Transactional
    public void processScheduledPayouts() {
        log.debug("Processing scheduled payouts");

        List<Payout> pendingPayouts = payoutRepository.findPendingForProcessing(LocalDateTime.now());

        for (Payout payout : pendingPayouts) {
            try {
                processPayout(payout);
            } catch (Exception e) {
                log.error("Failed to process payout {}: {}", payout.getReference(), e.getMessage());
                handlePayoutFailure(payout, e.getMessage());
            }
        }
    }

    @Override
    @Transactional
    public void retryFailedPayouts() {
        log.debug("Retrying failed payouts");

        List<Payout> failedPayouts = payoutRepository.findFailedForRetry(LocalDateTime.now(), maxRetryAttempts);

        for (Payout payout : failedPayouts) {
            try {
                log.info("Retrying payout {} (attempt {})", payout.getReference(), payout.getRetryCount() + 1);
                processPayout(payout);
            } catch (Exception e) {
                log.error("Retry failed for payout {}: {}", payout.getReference(), e.getMessage());
                handlePayoutFailure(payout, e.getMessage());
            }
        }
    }

    @Override
    public void handlePayoutWebhook(String interswitchRef, String status) {
        log.info("Processing payout webhook: {} with status: {}", interswitchRef, status);

        Payout payout = payoutRepository.findByInterswitchRef(interswitchRef)
                .orElseThrow(() -> new EntityNotFoundException("Payout not found with Interswitch ref: " + interswitchRef));

        PayoutStatus newStatus = mapInterswitchStatus(status);

        if (payout.getStatus() != newStatus) {
            payout.setStatus(newStatus);

            if (newStatus == PayoutStatus.COMPLETED) {
                payout.setCompletedAt(LocalDateTime.now());
                publishPayoutEvent(payout, "completed");
            } else if (newStatus == PayoutStatus.FAILED) {
                handlePayoutFailure(payout, "Webhook reported failure");
            }

            payoutRepository.save(payout);
        }
    }

    @Override
    @Transactional
    public void processPayoutImmediately(String reference) {
        log.info("Processing payout immediately: {}", reference);

        Payout payout = payoutRepository.findByReference(reference)
                .orElseThrow(() -> new EntityNotFoundException("Payout not found: " + reference));

        // Idempotency guard: only process if payout is in a processable state
        if (payout.getStatus() != PayoutStatus.PENDING) {
            log.warn("Payout {} already in progress or completed with status: {}", reference, payout.getStatus());
            return;
        }

        processPayout(payout);
    }

    private void processPayout(Payout payout) {
        log.info("Processing payout: {}", payout.getReference());

        // Idempotency guard
        if (payout.getStatus() != PayoutStatus.PENDING) {
            log.warn("Payout {} already in progress or completed with status: {}", payout.getReference(), payout.getStatus());
            return;
        }

        payout.setStatus(PayoutStatus.PROCESSING);
        payout.setProcessedAt(LocalDateTime.now());
        payoutRepository.save(payout);

        // Call Interswitch
        String interswitchRef = interswitchClient.initiatePayout(
                payout.getReference(),
                payout.getNetAmount(),
                payout.getBankCode(),
                payout.getAccountNumber(),
                payout.getAccountName()
        );

        payout.setInterswitchRef(interswitchRef);
        payout.setStatus(PayoutStatus.QUEUED);
        payoutRepository.save(payout);

        log.info("Payout {} submitted to Interswitch with ref: {}", payout.getReference(), interswitchRef);
    }

    private void handlePayoutFailure(Payout payout, String reason) {
        payout.setFailedAt(LocalDateTime.now());
        payout.setFailureReason(reason);
        payout.setRetryCount(payout.getRetryCount() + 1);

        if (payout.getRetryCount() >= maxRetryAttempts) {
            payout.setStatus(PayoutStatus.FAILED);
            log.error("Payout {} failed permanently after {} attempts: {}",
                    payout.getReference(), maxRetryAttempts, reason);
            publishPayoutEvent(payout, "failed");
        } else {
            // Schedule retry with exponential backoff
            int minutes = (int) Math.pow(2, payout.getRetryCount());
            payout.setNextRetryAt(LocalDateTime.now().plusMinutes(minutes));
            payout.setStatus(PayoutStatus.PENDING);
            log.warn("Payout {} failed, scheduled retry {} in {} minutes: {}",
                    payout.getReference(), payout.getRetryCount(), minutes, reason);
        }

        payoutRepository.save(payout);
    }

    private PayoutStatus mapInterswitchStatus(String status) {
        return switch (status.toUpperCase()) {
            case "SUCCESS", "COMPLETED" -> PayoutStatus.COMPLETED;
            case "FAILED", "REJECTED" -> PayoutStatus.FAILED;
            case "PENDING" -> PayoutStatus.PENDING;
            case "PROCESSING", "QUEUED" -> PayoutStatus.QUEUED;
            default -> PayoutStatus.FAILED;
        };
    }

    private String generateReference() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String random = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "POUT-" + timestamp + "-" + random;
    }

    private void publishPayoutEvent(Payout payout, String eventType) {
        PayoutEvent event = PayoutEvent.builder()
                .eventType("payout." + eventType)
                .reference(payout.getReference())
                .escrowReference(payout.getEscrowReference())
                .merchantId(payout.getMerchantId().toString())
                .amount(payout.getAmount())
                .fee(payout.getFee())
                .netAmount(payout.getNetAmount())
                .currency(payout.getCurrency())
                .status(payout.getStatus().toString())
                .bankCode(payout.getBankCode())
                .bankName(payout.getBankName())
                .accountNumber(payout.getAccountNumber())
                .accountName(payout.getAccountName())
                .timestamp(LocalDateTime.now())
                .build();

        rabbitTemplate.convertAndSend("eaas.exchange", "payout." + eventType, event);
    }

    private PayoutResponse mapToResponse(Payout payout) {
        return PayoutResponse.builder()
                .id(payout.getId())
                .reference(payout.getReference())
                .escrowReference(payout.getEscrowReference())
                .merchantId(payout.getMerchantId())
                .amount(payout.getAmount())
                .fee(payout.getFee())
                .netAmount(payout.getNetAmount())
                .currency(payout.getCurrency())
                .status(payout.getStatus())
                .method(payout.getMethod())
                .bankName(payout.getBankName())
                .accountNumber(maskAccountNumber(payout.getAccountNumber()))
                .accountName(payout.getAccountName())
                .scheduledAt(payout.getScheduledAt())
                .processedAt(payout.getProcessedAt())
                .completedAt(payout.getCompletedAt())
                .createdAt(payout.getCreatedAt())
                .build();
    }

    private String maskAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 4) return accountNumber;
        return "****" + accountNumber.substring(accountNumber.length() - 4);
    }
}
