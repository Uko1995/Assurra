package com.uko.eaas.identity.controller;

import com.uko.eaas.identity.dto.ApiKeyInfoResponse;
import com.uko.eaas.identity.dto.ApiResponse;
import com.uko.eaas.identity.dto.KycDocumentUploadResponse;
import com.uko.eaas.identity.dto.KycSubmitRequest;
import com.uko.eaas.identity.dto.KycSubmitResponse;
import com.uko.eaas.identity.dto.MerchantProfileResponse;
import com.uko.eaas.identity.dto.WebhookConfigRequest;
import com.uko.eaas.identity.dto.WebhookConfigResponse;
import com.uko.eaas.identity.model.enums.DocumentType;
import com.uko.eaas.identity.service.MerchantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.UUID;

/**
 * Merchant Controller
 * Provides merchant-facing endpoints for profile and KYC management.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/merchants")
@RequiredArgsConstructor
public class MerchantController {

    private final MerchantService merchantService;

    /**
     * Submit or resubmit KYC details.
     * Only authenticated merchants can access this endpoint.
     *
     * @param request   KYC submission details
     * @param principal Authenticated user principal (contains user UUID)
     * @return KYC submission response
     */
    @PostMapping("/kyc")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<ApiResponse<KycSubmitResponse>> submitKyc(
            @Valid @RequestBody KycSubmitRequest request,
            Principal principal) {

        UUID merchantId = UUID.fromString(principal.getName());
        log.info("KYC submission requested by merchant: {}", merchantId);

        KycSubmitResponse response = merchantService.submitKyc(merchantId, request);

        return ResponseEntity.ok(ApiResponse.success(response.getMessage(), response));
    }

    /**
     * Upload KYC document.
     * Only authenticated merchants can access this endpoint.
     */
    @PostMapping(value = "/kyc/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<ApiResponse<KycDocumentUploadResponse>> uploadKycDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("documentType") DocumentType documentType,
            Principal principal) {

        UUID merchantId = UUID.fromString(principal.getName());
        log.info("KYC document upload requested by merchant: {} type: {}", merchantId, documentType);

        KycDocumentUploadResponse response = merchantService.uploadKycDocument(merchantId, file, documentType);
        return ResponseEntity.ok(ApiResponse.success("KYC document uploaded successfully", response));
    }

    /**
     * Configure merchant webhook URL and events.
     */
    @PutMapping("/webhook")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<ApiResponse<WebhookConfigResponse>> configureWebhook(
            @Valid @RequestBody WebhookConfigRequest request,
            Principal principal) {

        UUID merchantId = UUID.fromString(principal.getName());
        log.info("Webhook configuration requested by merchant: {}", merchantId);

        WebhookConfigResponse response = merchantService.configureWebhook(merchantId, request);
        return ResponseEntity.ok(ApiResponse.success("Webhook configured successfully", response));
    }

    /**
     * Get the authenticated merchant's full profile.
     */
    @GetMapping("/profile")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<ApiResponse<MerchantProfileResponse>> getProfile(Principal principal) {
        UUID merchantId = UUID.fromString(principal.getName());
        log.info("Profile requested by merchant: {}", merchantId);

        MerchantProfileResponse response = merchantService.getProfile(merchantId);
        return ResponseEntity.ok(ApiResponse.success("Profile retrieved", response));
    }

    /**
     * Get the authenticated merchant's API key info (prefix only).
     */
    @GetMapping("/api-keys")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<ApiResponse<ApiKeyInfoResponse>> getApiKeyInfo(Principal principal) {
        UUID merchantId = UUID.fromString(principal.getName());
        log.info("API key info requested by merchant: {}", merchantId);

        ApiKeyInfoResponse response = merchantService.getApiKeyInfo(merchantId);
        return ResponseEntity.ok(ApiResponse.success("API key info retrieved", response));
    }

    /**
     * Regenerate merchant API key (self-service).
     * Invalidates the old key immediately.
     */
    @PostMapping("/api-key/regenerate")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<ApiResponse<String>> regenerateApiKey(Principal principal) {
        UUID merchantId = UUID.fromString(principal.getName());
        log.info("API key regeneration requested by merchant: {}", merchantId);

        String newApiKey = merchantService.regenerateMerchantApiKey(merchantId);
        return ResponseEntity.ok(ApiResponse.success(
                "API key regenerated successfully. Store this securely — it will not be shown again.", newApiKey));
    }
}
