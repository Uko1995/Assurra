package com.uko.eaas.escrow.controller;

import com.uko.eaas.escrow.dto.ApiResponse;
import com.uko.eaas.escrow.dto.EscrowResponse;
import com.uko.eaas.escrow.model.enums.EscrowStatus;
import com.uko.eaas.escrow.repository.EscrowTransactionRepository;
import com.uko.eaas.escrow.service.EscrowService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/admin/escrows")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminEscrowController {

    private final EscrowService escrowService;
    private final EscrowTransactionRepository escrowRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<EscrowResponse>>> listAll(
            @RequestParam(required = false) EscrowStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Pageable pageable) {

        Page<EscrowResponse> escrows;
        if (status != null) {
            escrows = escrowRepository.findByStatus(status, pageable).map(this::mapToResponse);
        } else {
            escrows = escrowRepository.findAll(pageable).map(this::mapToResponse);
        }
        return ResponseEntity.ok(ApiResponse.success(escrows));
    }

    @GetMapping("/{reference}")
    public ResponseEntity<ApiResponse<EscrowResponse>> getByReference(@PathVariable String reference) {
        EscrowResponse escrow = escrowService.getEscrow(reference);
        return ResponseEntity.ok(ApiResponse.success(escrow));
    }

    private EscrowResponse mapToResponse(com.uko.eaas.escrow.model.entity.EscrowTransaction escrow) {
        return EscrowResponse.builder()
                .id(escrow.getId())
                .reference(escrow.getReference())
                .customerId(escrow.getCustomerId())
                .merchantId(escrow.getMerchantId())
                .amount(escrow.getAmount())
                .escrowFee(escrow.getEscrowFee())
                .merchantAmount(escrow.getMerchantAmount())
                .currency(escrow.getCurrency())
                .status(escrow.getStatus())
                .productDescription(escrow.getProductDescription())
                .trackingNumber(escrow.getTrackingNumber())
                .shippedAt(escrow.getShippedAt())
                .deliveredAt(escrow.getDeliveredAt())
                .confirmedAt(escrow.getConfirmedAt())
                .createdAt(escrow.getCreatedAt())
                .build();
    }
}
