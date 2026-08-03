package com.uko.eaas.payment.controller;

import com.uko.eaas.payment.dto.*;
import com.uko.eaas.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<ApiResponse<InitializePaymentResponse>> initializePayment(
            @Valid @RequestBody InitializePaymentRequest request,
            @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey) {

        if ((request.getIdempotencyKey() == null || request.getIdempotencyKey().isBlank())
                && idempotencyKey != null && !idempotencyKey.isBlank()) {
            request.setIdempotencyKey(idempotencyKey);
        }

        InitializePaymentResponse response = paymentService.initializePayment(request);
        if (response.isReplayed()) {
            return ResponseEntity.ok()
                    .body(ApiResponse.success("Payment already exists for idempotency key. Returning cached payment.", response));
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payment initialized successfully", response));
    }

    @GetMapping("/{reference}")
    public ResponseEntity<ApiResponse<PaymentVerificationResponse>> getPayment(
            @PathVariable String reference) {

        PaymentVerificationResponse response = paymentService.getPayment(reference);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{reference}/verify")
    public ResponseEntity<ApiResponse<PaymentVerificationResponse>> verifyPayment(
            @PathVariable String reference) {

        PaymentVerificationResponse response = paymentService.verifyPayment(reference);
        return ResponseEntity.ok(ApiResponse.success("Payment verified", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<PaymentVerificationResponse>>> listPayments(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String role,
            Pageable pageable) {

        Page<PaymentVerificationResponse> payments = paymentService.listPayments(userId, role, pageable);
        return ResponseEntity.ok(ApiResponse.success(payments));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "payment-service");
        return ResponseEntity.ok(response);
    }
}
