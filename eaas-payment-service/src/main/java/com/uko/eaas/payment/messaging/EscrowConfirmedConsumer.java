package com.uko.eaas.payment.messaging;

import com.uko.eaas.payment.client.MerchantInternalClient;
import com.uko.eaas.payment.dto.CreatePayoutRequest;
import com.uko.eaas.payment.dto.MerchantSettlementDetailsResponse;
import com.uko.eaas.payment.dto.PayoutResponse;
import com.uko.eaas.payment.messaging.event.EscrowEvent;
import com.uko.eaas.payment.model.entity.PaymentTransaction;
import com.uko.eaas.payment.model.enums.PaymentStatus;
import com.uko.eaas.payment.repository.PaymentTransactionRepository;
import com.uko.eaas.payment.service.PaymentService;
import com.uko.eaas.payment.service.PayoutService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EscrowConfirmedConsumer {

    private final PayoutService payoutService;
    private final MerchantInternalClient merchantClient;
    private final PaymentTransactionRepository paymentRepository;
    private final PaymentService paymentService;

    @RabbitListener(queues = "payment.escrow.triggers")
    public void onEscrowConfirmed(EscrowEvent event) {
        log.info("Received escrow event: {} for escrow: {}", event.getEventType(), event.getReference());

        try {
            switch (event.getEventType()) {
                case "escrow.confirmed", "escrow.auto-released", "escrow.resolved-merchant" -> releaseFunds(event);
                case "escrow.resolved-customer" -> refundCustomer(event);
                default -> log.debug("Ignoring event type: {}", event.getEventType());
            }
        } catch (Exception e) {
            log.error("Failed to process escrow event {} for escrow {}: {}",
                    event.getEventType(), event.getReference(), e.getMessage(), e);
            throw e; // Let Spring retry/DLQ handle it
        }
    }

    private void releaseFunds(EscrowEvent event) {
        // Fetch merchant settlement details from Identity Service
        MerchantSettlementDetailsResponse details = merchantClient.getSettlementDetails(event.getMerchantId());

        // Build payout request
        CreatePayoutRequest request = new CreatePayoutRequest();
        request.setEscrowReference(event.getReference());
        request.setMerchantId(event.getMerchantId());
        request.setAmount(event.getMerchantAmount());
        request.setBankCode(details.getBankCode());
        request.setBankName(details.getBankName());
        request.setAccountNumber(details.getAccountNumber());
        request.setAccountName(details.getAccountName());

        // Create payout (idempotent: throws if escrowReference already exists)
        PayoutResponse payout;
        try {
            payout = payoutService.createPayout(request);
            log.info("Created payout {} for escrow: {}", payout.getReference(), event.getReference());
        } catch (IllegalStateException e) {
            // Payout already exists for this escrow — idempotent guard
            log.warn("Payout already exists for escrow {}: {}", event.getReference(), e.getMessage());
            // Try to fetch existing payout and process it
            payout = payoutService.getPayoutByEscrow(event.getReference());
        }

        // Immediately process the payout
        payoutService.processPayoutImmediately(payout.getReference());
        log.info("Payout {} processed immediately for escrow: {}", payout.getReference(), event.getReference());
    }

    private void refundCustomer(EscrowEvent event) {
        PaymentTransaction tx = paymentRepository.findByEscrowReference(event.getReference())
                .orElseThrow(() -> new IllegalStateException("No payment found for escrow: " + event.getReference()));

        if (tx.getStatus() == PaymentStatus.REFUNDED) {
            log.info("Payment {} already refunded, ignoring duplicate resolution event", tx.getReference());
            return;
        }

        paymentService.processRefund(tx.getReference(), "Dispute resolved in customer's favor");
        log.info("Refunded payment {} for escrow: {}", tx.getReference(), event.getReference());
    }
}