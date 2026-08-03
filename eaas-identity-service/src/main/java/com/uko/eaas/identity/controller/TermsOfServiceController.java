package com.uko.eaas.identity.controller;

import com.uko.eaas.identity.dto.ApiResponse;
import com.uko.eaas.identity.model.entity.TermsOfService;
import com.uko.eaas.identity.repository.TermsOfServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/terms-of-service")
@RequiredArgsConstructor
public class TermsOfServiceController {

    private final TermsOfServiceRepository termsOfServiceRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<TermsOfService>> getLatestTermsOfService() {
        TermsOfService terms = termsOfServiceRepository.findFirstByIsActiveTrueOrderByEffectiveFromDesc()
                .orElseThrow(() -> new RuntimeException("No active terms of service found"));
        return ResponseEntity.ok(ApiResponse.success(terms));
    }
}
