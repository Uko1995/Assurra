package com.uko.eaas.escrow.service;

import com.uko.eaas.escrow.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EscrowService {

    EscrowResponse createEscrow(CreateEscrowRequest request, String customerId, String idempotencyKey);

    EscrowResponse getEscrow(String reference);

    Page<EscrowResponse> listAllEscrows(String userId, String role, Pageable pageable);


    EscrowResponse shipEscrow(String reference, ShipEscrowRequest request);

    EscrowResponse deliverEscrow(String reference, String customerId);

    EscrowResponse confirmEscrow(String reference, String customerId);

    EscrowResponse raiseDispute(String reference);

    EscrowResponse resolveDispute(String reference, String resolution);

    void cancelEscrow(String reference, String userId, String role);

    void handlePaymentWebhook(PaymentWebhookRequest request);

    void expireUnfundedEscrows();

    void autoReleaseEscrows();
}
