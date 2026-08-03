package com.uko.eaas.identity.controller;

import com.uko.eaas.identity.dto.ApiResponse;
import com.uko.eaas.identity.model.entity.PrivacyPolicy;
import com.uko.eaas.identity.repository.PrivacyPolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/privacy-policy")
@RequiredArgsConstructor
public class PrivacyPolicyController {

    private final PrivacyPolicyRepository privacyPolicyRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<PrivacyPolicy>> getLatestPrivacyPolicy() {
        PrivacyPolicy policy = privacyPolicyRepository.findFirstByIsActiveTrueOrderByEffectiveFromDesc()
                .orElseThrow(() -> new RuntimeException("No active privacy policy found"));
        return ResponseEntity.ok(ApiResponse.success(policy));
    }
}
