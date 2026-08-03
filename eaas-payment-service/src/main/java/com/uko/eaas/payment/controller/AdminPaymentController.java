package com.uko.eaas.payment.controller;

import com.uko.eaas.payment.dto.ApiResponse;
import com.uko.eaas.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/payments")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminPaymentController {

    private final PaymentService paymentService;

    @PostMapping("/{reference}/refund")
    public ResponseEntity<ApiResponse<Void>> processRefund(
            @PathVariable String reference,
            @RequestParam String reason) {

        paymentService.processRefund(reference, reason);
        return ResponseEntity.ok(ApiResponse.success("Refund processed successfully", null));
    }
}
