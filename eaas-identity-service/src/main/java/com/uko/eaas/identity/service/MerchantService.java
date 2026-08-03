package com.uko.eaas.identity.service;

import com.uko.eaas.identity.dto.ApiKeyInfoResponse;
import com.uko.eaas.identity.dto.KycDocumentUploadResponse;
import com.uko.eaas.identity.dto.KycSubmitRequest;
import com.uko.eaas.identity.dto.KycSubmitResponse;
import com.uko.eaas.identity.dto.MerchantProfileResponse;
import com.uko.eaas.identity.dto.WebhookConfigRequest;
import com.uko.eaas.identity.dto.WebhookConfigResponse;
import com.uko.eaas.identity.model.enums.DocumentType;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * Merchant Service Interface
 * Provides merchant-facing operations for profile and KYC management.
 */
public interface MerchantService {

    /**
     * Submit or resubmit KYC details for the authenticated merchant.
     *
     * @param merchantId The merchant's user ID
     * @param request    KYC submission details
     * @return KYC submission response with updated status
     */
    KycSubmitResponse submitKyc(UUID merchantId, KycSubmitRequest request);

    /**
     * Upload a KYC document for the authenticated merchant.
     *
     * @param merchantId   The merchant's user ID
     * @param file         The document file
     * @param documentType The type of document
     * @return KYC document upload response
     */
    KycDocumentUploadResponse uploadKycDocument(UUID merchantId, MultipartFile file, DocumentType documentType);

    /**
     * Configure merchant webhook URL and events.
     *
     * @param merchantId The merchant's user ID
     * @param request    Webhook configuration request
     * @return Webhook configuration response
     */
    WebhookConfigResponse configureWebhook(UUID merchantId, WebhookConfigRequest request);

    /**
     * Get the authenticated merchant's full profile.
     *
     * @param merchantId The merchant's user ID
     * @return Merchant profile response
     */
    MerchantProfileResponse getProfile(UUID merchantId);

    /**
     * Get the authenticated merchant's API key info (prefix only, not full key).
     *
     * @param merchantId The merchant's user ID
     * @return API key info response
     */
    ApiKeyInfoResponse getApiKeyInfo(UUID merchantId);

    /**
     * Regenerate the merchant's API key (self-service).
     *
     * @param merchantId The merchant's user ID
     * @return The new plain API key
     */
    String regenerateMerchantApiKey(UUID merchantId);
}
