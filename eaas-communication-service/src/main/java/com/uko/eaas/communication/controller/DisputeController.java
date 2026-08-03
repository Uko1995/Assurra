package com.uko.eaas.communication.controller;

import com.uko.eaas.communication.dto.*;
import com.uko.eaas.communication.model.enums.DisputeStatus;
import com.uko.eaas.communication.service.DisputeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/disputes")
@RequiredArgsConstructor
public class DisputeController {

    private final DisputeService disputeService;

    @PostMapping
    public ResponseEntity<ApiResponse<DisputeResponse>> createDispute(
            @Valid @RequestBody CreateDisputeRequest request) {

        DisputeResponse response = disputeService.createDispute(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Dispute created successfully", response));
    }

    @GetMapping("/{reference}")
    public ResponseEntity<ApiResponse<DisputeResponse>> getDispute(
            @PathVariable String reference) {

        DisputeResponse response = disputeService.getDispute(reference);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/by-escrow/{escrowReference}")
    public ResponseEntity<ApiResponse<DisputeResponse>> getDisputeByEscrow(
            @PathVariable String escrowReference) {

        DisputeResponse response = disputeService.getDisputeByEscrow(escrowReference);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<DisputeResponse>>> listDisputes(
            @RequestHeader("X-User-Id") String userId,
            Pageable pageable) {

        Page<DisputeResponse> disputes = disputeService.listDisputes(UUID.fromString(userId), pageable);
        return ResponseEntity.ok(ApiResponse.success(disputes));
    }

    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<DisputeResponse>>> listActiveDisputes(Pageable pageable) {
        Page<DisputeResponse> disputes = disputeService.listActiveDisputes(pageable);
        return ResponseEntity.ok(ApiResponse.success(disputes));
    }

    @PostMapping("/{reference}/resolve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DisputeResponse>> resolveDispute(
            @PathVariable String reference,
            @Valid @RequestBody ResolveDisputeRequest request,
            @RequestHeader("X-User-Id") String resolvedBy) {

        DisputeResponse response = disputeService.resolveDispute(reference, request, UUID.fromString(resolvedBy));
        return ResponseEntity.ok(ApiResponse.success("Dispute resolved successfully", response));
    }

    @PutMapping("/{reference}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DisputeResponse>> updateDisputeStatus(
            @PathVariable String reference,
            @RequestParam DisputeStatus status) {

        DisputeResponse response = disputeService.updateDisputeStatus(reference, status);
        return ResponseEntity.ok(ApiResponse.success("Dispute status updated", response));
    }

    // Messages
    @PostMapping("/{disputeId}/messages")
    public ResponseEntity<ApiResponse<DisputeMessageResponse>> addMessage(
            @PathVariable UUID disputeId,
            @Valid @RequestBody DisputeMessageRequest request) {

        request.setDisputeId(disputeId);
        DisputeMessageResponse response = disputeService.addMessage(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Message added successfully", response));
    }

    @GetMapping("/{disputeId}/messages")
    public ResponseEntity<ApiResponse<Page<DisputeMessageResponse>>> getMessages(
            @PathVariable UUID disputeId,
            Pageable pageable) {

        Page<DisputeMessageResponse> messages = disputeService.getMessages(disputeId, pageable);
        return ResponseEntity.ok(ApiResponse.success(messages));
    }

    @PutMapping("/{disputeId}/messages/read")
    public ResponseEntity<ApiResponse<Void>> markMessagesAsRead(
            @PathVariable UUID disputeId,
            @RequestParam String readerType) {

        disputeService.markMessagesAsRead(disputeId, readerType);
        return ResponseEntity.ok(ApiResponse.success("Messages marked as read", null));
    }

    // Evidence
    @PostMapping("/{disputeId}/evidence")
    public ResponseEntity<ApiResponse<EvidenceResponse>> uploadEvidence(
            @PathVariable UUID disputeId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String evidenceType,
            @RequestHeader("X-User-Id") String uploadedBy) {

        EvidenceResponse response = disputeService.uploadEvidence(
                disputeId, UUID.fromString(uploadedBy), file, description, evidenceType);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Evidence uploaded successfully", response));
    }

    @DeleteMapping("/evidence/{evidenceId}")
    public ResponseEntity<ApiResponse<Void>> deleteEvidence(
            @PathVariable UUID evidenceId,
            @RequestHeader("X-User-Id") String deletedBy) {

        disputeService.deleteEvidence(evidenceId, UUID.fromString(deletedBy));
        return ResponseEntity.ok(ApiResponse.success("Evidence deleted successfully", null));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "dispute-service");
        return ResponseEntity.ok(response);
    }
}
