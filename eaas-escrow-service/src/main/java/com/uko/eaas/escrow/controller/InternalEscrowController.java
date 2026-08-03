package com.uko.eaas.escrow.controller;

import com.uko.eaas.escrow.dto.ApiResponse;
import com.uko.eaas.escrow.dto.EscrowDisputeResolutionRequest;
import com.uko.eaas.escrow.dto.EscrowResponse;
import com.uko.eaas.escrow.service.EscrowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/internal/api/v1/escrow")
@RequiredArgsConstructor
public class InternalEscrowController {

    private final EscrowService escrowService;

    @GetMapping("/{reference}")
    public ResponseEntity<ApiResponse<EscrowResponse>> getEscrow(@PathVariable String reference) {
        log.debug("Internal request for escrow: {}", reference);
        EscrowResponse escrow = escrowService.getEscrow(reference);
        return ResponseEntity.ok(ApiResponse.success(escrow));
    }

    @PostMapping("/{reference}/dispute")
    public ResponseEntity<ApiResponse<EscrowResponse>> raiseDispute(@PathVariable String reference) {
        log.debug("Internal request to raise dispute for escrow: {}", reference);
        EscrowResponse escrow = escrowService.raiseDispute(reference);
        return ResponseEntity.ok(ApiResponse.success(escrow));
    }

    @PostMapping("/{reference}/dispute/resolve")
    public ResponseEntity<ApiResponse<EscrowResponse>> resolveDispute(
            @PathVariable String reference,
            @Valid @RequestBody EscrowDisputeResolutionRequest request) {
        log.debug("Internal request to resolve dispute for escrow: {}", reference);
        EscrowResponse escrow = escrowService.resolveDispute(reference, request.getResolution());
        return ResponseEntity.ok(ApiResponse.success(escrow));
    }
}
