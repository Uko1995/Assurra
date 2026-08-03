package com.uko.eaas.payment.service;

import com.uko.eaas.payment.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaymentService {

    InitializePaymentResponse initializePayment(InitializePaymentRequest request);

    PaymentVerificationResponse verifyPayment(String reference);

    PaymentVerificationResponse getPayment(String reference);

    Page<PaymentVerificationResponse> listPayments(String userId, String role, Pageable pageable);

    void handleWebhook(PaymentWebhookPayload payload, String signature);

    void processRefund(String reference, String reason);
}
