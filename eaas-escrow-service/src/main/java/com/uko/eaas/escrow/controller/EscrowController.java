package com.uko.eaas.escrow.controller;

import com.uko.eaas.escrow.dto.*;
import com.uko.eaas.escrow.service.EscrowService;
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
@RequestMapping("/api/v1/escrow")
@RequiredArgsConstructor
public class EscrowController {

    private final EscrowService escrowService;

    @PostMapping
    public ResponseEntity<ApiResponse<EscrowResponse>> createEscrow(
            @Valid @RequestBody CreateEscrowRequest request,
            @RequestHeader("X-User-Id") String customerId,
            @RequestHeader(value = "X-Idempotency-Key") String idempotencyKey) {
        
        EscrowResponse escrow = escrowService.createEscrow(request, customerId, idempotencyKey);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Escrow created successfully", escrow));
    }

    @GetMapping("/{reference}")
    public ResponseEntity<ApiResponse<EscrowResponse>> getEscrow(
            @PathVariable String reference) {
        EscrowResponse escrow = escrowService.getEscrow(reference);
        return ResponseEntity.ok(ApiResponse.success(escrow));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<EscrowResponse>>> listEscrows(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String role,
            Pageable pageable) {
        
        Page<EscrowResponse> escrows = escrowService.listAllEscrows(userId, role, pageable);
        return ResponseEntity.ok(ApiResponse.success(escrows));
    }

    @PostMapping("/{reference}/ship")
    public ResponseEntity<ApiResponse<EscrowResponse>> shipEscrow(
            @PathVariable String reference,
            @Valid @RequestBody ShipEscrowRequest request) {
        EscrowResponse escrow = escrowService.shipEscrow(reference, request);
        return ResponseEntity.ok(ApiResponse.success("Escrow marked as shipped", escrow));
    }

    @PostMapping("/{reference}/deliver")
    public ResponseEntity<ApiResponse<EscrowResponse>> deliverEscrow(
            @PathVariable String reference,
            @RequestHeader("X-User-Id") String customerId) {
        EscrowResponse escrow = escrowService.deliverEscrow(reference, customerId);
        return ResponseEntity.ok(ApiResponse.success("Escrow marked as delivered", escrow));
    }

    @PostMapping("/{reference}/confirm")
    public ResponseEntity<ApiResponse<EscrowResponse>> confirmEscrow(
            @PathVariable String reference,
            @RequestHeader("X-User-Id") String customerId) {
        EscrowResponse escrow = escrowService.confirmEscrow(reference, customerId);
        return ResponseEntity.ok(ApiResponse.success("Escrow confirmed. Payout will be processed within 24 hours.", escrow));
    }

    @PostMapping("/{reference}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelEscrow(
            @PathVariable String reference,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String role) {
        escrowService.cancelEscrow(reference, userId, role);
        return ResponseEntity.ok(ApiResponse.success("Escrow cancelled successfully", null));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "escrow-service");
        return ResponseEntity.ok(response);
    }
}
