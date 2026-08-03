package com.uko.eaas.identity.controller;

import com.uko.eaas.identity.dto.MerchantSettlementDetailsResponse;
import com.uko.eaas.identity.model.entity.MerchantProfile;
import com.uko.eaas.identity.repository.MerchantProfileRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Internal API for service-to-service communication.
 * Exposes merchant settlement details to the Payment Service for payout processing.
 * Secured by internal network isolation; called only from within the Docker network.
 */
@Slf4j
@RestController
@RequestMapping("/internal/merchants")
@RequiredArgsConstructor
public class InternalMerchantController {

    private final MerchantProfileRepository merchantProfileRepository;

    @GetMapping("/{merchantId}/settlement-details")
    public ResponseEntity<MerchantSettlementDetailsResponse> getSettlementDetails(
            @PathVariable UUID merchantId) {
        log.debug("Internal request for merchant settlement details: {}", merchantId);

        // merchantId is the user UUID (escrow/merchant identifiers reference users);
        // the merchant profile itself has its own id, so look up by user id.
        MerchantProfile profile = merchantProfileRepository.findByUserId(merchantId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException(
                        "Merchant profile not found: " + merchantId));

        MerchantSettlementDetailsResponse response = MerchantSettlementDetailsResponse.builder()
                .merchantId(profile.getId().toString())
                .businessName(profile.getBusinessName())
                .bankCode(profile.getBankCode())
                .bankName(profile.getBankName())
                .accountNumber(profile.getBankAccountNumber())
                .accountName(profile.getBusinessName())
                .settlementEmail(profile.getSettlementEmail())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{merchantId}/webhook-config")
    public ResponseEntity<WebhookConfigResponse> getWebhookConfig(@PathVariable UUID merchantId) {
        log.debug("Internal request for merchant webhook config: {}", merchantId);

        // merchantId is the user UUID (see settlement-details above)
        MerchantProfile profile = merchantProfileRepository.findByUserId(merchantId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException(
                        "Merchant profile not found: " + merchantId));

        WebhookConfigResponse response = new WebhookConfigResponse();
        response.setWebhookUrl(profile.getWebhookUrl());
        // Note: webhook_secret is now hashed with BCrypt for security.
        // The raw secret is never returned. HMAC signing is handled via
        // a signing key stored in the communication service (see webhook engine architecture).
        response.setWebhookSecret(null);
        return ResponseEntity.ok(response);
    }

    @Data
    public static class WebhookConfigResponse {
        private String webhookUrl;
        private String webhookSecret;
    }
}
