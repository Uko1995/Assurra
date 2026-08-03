package com.uko.eaas.payment.controller;

import com.uko.eaas.payment.dto.ApiResponse;
import com.uko.eaas.payment.model.entity.AmlAlert;
import com.uko.eaas.payment.repository.AmlAlertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/aml-alerts")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminAmlController {

    private final AmlAlertRepository amlAlertRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<AmlAlert>>> listAlerts(
            @RequestParam(required = false) String status,
            Pageable pageable) {
        Page<AmlAlert> alerts = status != null
                ? amlAlertRepository.findByStatus(status, pageable)
                : amlAlertRepository.findAll(pageable);
        return ResponseEntity.ok(ApiResponse.success(alerts));
    }

    @PutMapping("/{id}/review")
    public ResponseEntity<ApiResponse<AmlAlert>> reviewAlert(
            @PathVariable UUID id,
            @RequestHeader("X-User-Id") String reviewedBy,
            @RequestParam String notes) {
        AmlAlert alert = amlAlertRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("AML alert not found: " + id));
        alert.setStatus("UNDER_REVIEW");
        alert.setReviewedBy(UUID.fromString(reviewedBy));
        alert.setReviewedAt(java.time.LocalDateTime.now());
        alert.setNotes(alert.getNotes() + "\n[REVIEW] " + notes);
        amlAlertRepository.save(alert);
        return ResponseEntity.ok(ApiResponse.success("Alert under review", alert));
    }

    @PutMapping("/{id}/resolve")
    public ResponseEntity<ApiResponse<AmlAlert>> resolveAlert(
            @PathVariable UUID id,
            @RequestParam String resolution,
            @RequestParam String notes) {
        AmlAlert alert = amlAlertRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("AML alert not found: " + id));
        alert.setStatus(resolution);
        alert.setNotes(alert.getNotes() + "\n[RESOLVED: " + resolution + "] " + notes);
        amlAlertRepository.save(alert);
        return ResponseEntity.ok(ApiResponse.success("Alert resolved", alert));
    }
}
