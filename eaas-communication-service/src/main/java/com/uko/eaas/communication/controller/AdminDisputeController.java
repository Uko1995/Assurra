package com.uko.eaas.communication.controller;

import com.uko.eaas.communication.dto.ApiResponse;
import com.uko.eaas.communication.dto.DisputeResponse;
import com.uko.eaas.communication.dto.ResolveDisputeRequest;
import com.uko.eaas.communication.model.enums.DisputeStatus;
import com.uko.eaas.communication.service.DisputeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/disputes")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminDisputeController {

    private final DisputeService disputeService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<DisputeResponse>>> listAll(
            @RequestParam(required = false) DisputeStatus status,
            Pageable pageable) {
        Page<DisputeResponse> disputes = disputeService.listActiveDisputes(pageable);
        return ResponseEntity.ok(ApiResponse.success(disputes));
    }

    @GetMapping("/{reference}")
    public ResponseEntity<ApiResponse<DisputeResponse>> getByReference(@PathVariable String reference) {
        DisputeResponse dispute = disputeService.getDispute(reference);
        return ResponseEntity.ok(ApiResponse.success(dispute));
    }

    @PutMapping("/{reference}/resolve")
    public ResponseEntity<ApiResponse<DisputeResponse>> resolve(
            @PathVariable String reference,
            @Valid @RequestBody ResolveDisputeRequest request,
            @RequestHeader("X-User-Id") String resolvedBy) {
        DisputeResponse response = disputeService.resolveDispute(reference, request, UUID.fromString(resolvedBy));
        return ResponseEntity.ok(ApiResponse.success("Dispute resolved successfully", response));
    }

    @PutMapping("/{reference}/status")
    public ResponseEntity<ApiResponse<DisputeResponse>> updateStatus(
            @PathVariable String reference,
            @RequestParam DisputeStatus status) {
        DisputeResponse response = disputeService.updateDisputeStatus(reference, status);
        return ResponseEntity.ok(ApiResponse.success("Dispute status updated", response));
    }
}
