package com.uko.eaas.payment.controller;

import com.uko.eaas.payment.dto.*;
import com.uko.eaas.payment.service.PayoutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/payouts")
@RequiredArgsConstructor
public class PayoutController {

    private final PayoutService payoutService;

    @PostMapping
    public ResponseEntity<ApiResponse<PayoutResponse>> createPayout(
            @Valid @RequestBody CreatePayoutRequest request) {

        PayoutResponse response = payoutService.createPayout(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payout created successfully", response));
    }

    @GetMapping("/{reference}")
    public ResponseEntity<ApiResponse<PayoutResponse>> getPayout(
            @PathVariable String reference) {

        PayoutResponse response = payoutService.getPayout(reference);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/by-escrow/{escrowReference}")
    public ResponseEntity<ApiResponse<PayoutResponse>> getPayoutByEscrow(
            @PathVariable String escrowReference) {

        PayoutResponse response = payoutService.getPayoutByEscrow(escrowReference);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<PayoutResponse>>> listPayouts(
            @RequestHeader("X-User-Id") String merchantId,
            Pageable pageable) {

        Page<PayoutResponse> payouts = payoutService.listPayouts(UUID.fromString(merchantId), pageable);
        return ResponseEntity.ok(ApiResponse.success(payouts));
    }
}
