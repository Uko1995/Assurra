package com.uko.eaas.payment.controller;

import com.uko.eaas.payment.dto.ApiResponse;
import com.uko.eaas.payment.dto.PayoutResponse;
import com.uko.eaas.payment.service.PayoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/payouts")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminPayoutController {

    private final PayoutService payoutService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<PayoutResponse>>> listAll(Pageable pageable) {
        Page<PayoutResponse> payouts = payoutService.listAllPayouts(pageable);
        return ResponseEntity.ok(ApiResponse.success(payouts));
    }

    @GetMapping("/{reference}")
    public ResponseEntity<ApiResponse<PayoutResponse>> getByReference(@PathVariable String reference) {
        PayoutResponse payout = payoutService.getPayout(reference);
        return ResponseEntity.ok(ApiResponse.success(payout));
    }

    @PostMapping("/{reference}/retry")
    public ResponseEntity<ApiResponse<PayoutResponse>> retryFailedPayout(@PathVariable String reference) {
        PayoutResponse payout = payoutService.retryPayout(reference);
        return ResponseEntity.ok(ApiResponse.success("Payout retry initiated", payout));
    }
}
