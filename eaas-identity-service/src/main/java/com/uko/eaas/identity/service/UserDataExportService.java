package com.uko.eaas.identity.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uko.eaas.identity.model.entity.User;
import com.uko.eaas.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Service for GDPR/NDPR data portability — exports all user data as a structured map.
 * In production, this would aggregate data from all services via RabbitMQ/internal APIs.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserDataExportService {

    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @SneakyThrows
    public String exportUserData(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        Map<String, Object> export = new HashMap<>();
        export.put("userId", user.getId());
        export.put("fullName", user.getFullName());
        export.put("email", user.getEmail());
        export.put("phone", user.getPhone());
        export.put("role", user.getRole());
        export.put("kycStatus", user.getKycStatus());
        export.put("emailVerified", user.getEmailVerified());
        export.put("isActive", user.getIsActive());
        export.put("consentGiven", user.getConsentGiven());
        export.put("consentGivenAt", user.getConsentGivenAt());
        export.put("termsAccepted", user.getTermsAccepted());
        export.put("termsAcceptedAt", user.getTermsAcceptedAt());
        export.put("privacyPolicyVersion", user.getPrivacyPolicyVersion());
        export.put("marketingConsent", user.getMarketingConsent());
        export.put("dataProcessingConsent", user.getDataProcessingConsent());
        export.put("createdAt", user.getCreatedAt());
        export.put("lastLoginAt", user.getLastLoginAt());
        export.put("exportGeneratedAt", java.time.LocalDateTime.now());
        export.put("notice", "This export contains identity-service data only. Escrow, payment, and communication data must be requested separately.");

        // In production: publish async requests to other services and aggregate responses
        // escrow-service: GET /internal/users/{userId}/escrows
        // payment-service: GET /internal/users/{userId}/payments
        // communication-service: GET /internal/users/{userId}/notifications

        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(export);
    }
}
