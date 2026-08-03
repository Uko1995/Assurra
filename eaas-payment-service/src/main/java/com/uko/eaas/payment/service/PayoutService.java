package com.uko.eaas.payment.service;

import com.uko.eaas.payment.dto.CreatePayoutRequest;
import com.uko.eaas.payment.dto.PayoutResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface PayoutService {

    PayoutResponse createPayout(CreatePayoutRequest request);

    PayoutResponse getPayout(String reference);

    PayoutResponse getPayoutByEscrow(String escrowReference);

    Page<PayoutResponse> listPayouts(UUID merchantId, Pageable pageable);

    Page<PayoutResponse> listAllPayouts(Pageable pageable);

    PayoutResponse retryPayout(String reference);

    void processScheduledPayouts();

    void retryFailedPayouts();

    void handlePayoutWebhook(String interswitchRef, String status);

    /**
     * Process a payout immediately by reference.
     * Used by the real-time RabbitMQ consumer to trigger payouts on escrow confirmation.
     *
     * @param reference the payout reference
     */
    void processPayoutImmediately(String reference);
}
