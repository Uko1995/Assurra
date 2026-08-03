package com.uko.eaas.identity.service;

import com.uko.eaas.identity.dto.ApiKeyResponse;
import com.uko.eaas.identity.dto.KycPendingMerchantResponse;
import com.uko.eaas.identity.dto.KycReviewRequest;
import com.uko.eaas.identity.dto.KycReviewResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Admin KYC Service Interface
 * Provides operations for administrators to manage merchant KYC
 */
public interface AdminKycService {
    
    /**
     * Get all merchants pending KYC review
     * 
     * @param pageable pagination info
     * @return Page of merchants pending KYC review
     */
    Page<KycPendingMerchantResponse> getPendingKycMerchants(Pageable pageable);
    
    /**
     * Get all merchants with KYC under review
     * 
     * @param pageable pagination info
     * @return Page of merchants under review
     */
    Page<KycPendingMerchantResponse> getUnderReviewMerchants(Pageable pageable);
    
    /**
     * Get merchant details for KYC review
     * 
     * @param merchantId the merchant user ID
     * @return Merchant details
     */
    KycPendingMerchantResponse getMerchantForReview(UUID merchantId);
    
    /**
     * Approve merchant KYC
     * Generates API key upon approval
     * 
     * @param request review request with merchant ID
     * @param adminId ID of the admin performing the review
     * @return Review response with API key (shown only once)
     */
    KycReviewResponse approveKyc(KycReviewRequest request, UUID adminId);
    
    /**
     * Reject merchant KYC
     * 
     * @param request review request with merchant ID and reason
     * @param adminId ID of the admin performing the review
     * @return Review response
     */
    KycReviewResponse rejectKyc(KycReviewRequest request, UUID adminId);
    
    /**
     * Put merchant KYC under manual review
     * 
     * @param merchantId the merchant user ID
     * @param adminId ID of the admin
     * @param notes review notes
     * @return Review response
     */
    KycReviewResponse putUnderReview(UUID merchantId, UUID adminId, String notes);

    ApiKeyResponse regenerateApiKey(UUID merchantId, UUID adminId, String reason);
}
