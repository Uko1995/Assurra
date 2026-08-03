package com.uko.eaas.escrow.controller;

import com.uko.eaas.escrow.dto.ApiResponse;
import com.uko.eaas.escrow.dto.CreateFeeConfigRequest;
import com.uko.eaas.escrow.dto.FeeConfigResponse;
import com.uko.eaas.escrow.model.entity.FeeConfiguration;
import com.uko.eaas.escrow.repository.FeeConfigurationRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/fee-configurations")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminFeeConfigController {

    private final FeeConfigurationRepository feeConfigurationRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<FeeConfigResponse>>> list(Pageable pageable) {
        Page<FeeConfigResponse> configs = feeConfigurationRepository.findAll(pageable)
                .map(this::mapToResponse);
        return ResponseEntity.ok(ApiResponse.success(configs));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<FeeConfigResponse>> create(
            @Valid @RequestBody CreateFeeConfigRequest request) {

        FeeConfiguration config = FeeConfiguration.builder()
                .merchantId(request.getMerchantId())
                .feeType(request.getFeeType())
                .feeValue(request.getFeeValue())
                .minFee(request.getMinFee())
                .maxFee(request.getMaxFee())
                .isActive(true)
                .effectiveFrom(request.getEffectiveFrom())
                .effectiveUntil(request.getEffectiveUntil())
                .build();

        config = feeConfigurationRepository.save(config);
        return ResponseEntity.ok(ApiResponse.success("Fee configuration created", mapToResponse(config)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<FeeConfigResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody CreateFeeConfigRequest request) {

        FeeConfiguration config = feeConfigurationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fee configuration not found: " + id));

        config.setFeeType(request.getFeeType());
        config.setFeeValue(request.getFeeValue());
        config.setMinFee(request.getMinFee());
        config.setMaxFee(request.getMaxFee());
        config.setEffectiveFrom(request.getEffectiveFrom());
        config.setEffectiveUntil(request.getEffectiveUntil());

        config = feeConfigurationRepository.save(config);
        return ResponseEntity.ok(ApiResponse.success("Fee configuration updated", mapToResponse(config)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable UUID id) {
        FeeConfiguration config = feeConfigurationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fee configuration not found: " + id));
        config.setIsActive(false);
        feeConfigurationRepository.save(config);
        return ResponseEntity.ok(ApiResponse.success("Fee configuration deactivated", null));
    }

    private FeeConfigResponse mapToResponse(FeeConfiguration config) {
        return FeeConfigResponse.builder()
                .id(config.getId())
                .merchantId(config.getMerchantId())
                .feeType(config.getFeeType())
                .feeValue(config.getFeeValue())
                .minFee(config.getMinFee())
                .maxFee(config.getMaxFee())
                .isActive(config.getIsActive())
                .effectiveFrom(config.getEffectiveFrom())
                .effectiveUntil(config.getEffectiveUntil())
                .createdAt(config.getCreatedAt())
                .updatedAt(config.getUpdatedAt())
                .build();
    }
}
