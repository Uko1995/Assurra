package com.uko.eaas.identity.controller;

import com.uko.eaas.identity.dto.*;
import com.uko.eaas.identity.service.AdminKycService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

/**
 * Admin Controller
 * Provides administrative endpoints for KYC management and merchant oversight.
 * All endpoints require ADMIN role.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminKycService adminKycService;

    /**
     * Get all merchants pending KYC review
     * 
     * @param pageable pagination parameters (page, size, sort)
     * @return Paginated list of merchants pending KYC
     * 
     * Sample Request:
     * GET /api/v1/admin/kyc/pending?page=0&size=10&sort=kycSubmittedAt,desc
     * 
     * Headers:
     * Authorization: Bearer {admin-jwt-token}
     */
    @GetMapping("/kyc/pending")
    public ResponseEntity<ApiResponse<Page<KycPendingMerchantResponse>>> getPendingKyc(
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        log.info("Admin fetching pending KYC merchants");
        Page<KycPendingMerchantResponse> pending = adminKycService.getPendingKycMerchants(pageable);
        return ResponseEntity.ok(ApiResponse.success(
                String.format("Found %d merchants pending KYC review", pending.getTotalElements()), 
                pending));
    }

    /**
     * Get all merchants under manual KYC review
     * 
     * @param pageable pagination parameters
     * @return Paginated list of merchants under review
     * 
     * GET /api/v1/admin/kyc/under-review
     */
    @GetMapping("/kyc/under-review")
    public ResponseEntity<ApiResponse<Page<KycPendingMerchantResponse>>> getUnderReview(
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        log.info("Admin fetching merchants under KYC review");
        Page<KycPendingMerchantResponse> underReview = adminKycService.getUnderReviewMerchants(pageable);
        return ResponseEntity.ok(ApiResponse.success(
                String.format("Found %d merchants under review", underReview.getTotalElements()), 
                underReview));
    }

    /**
     * Get detailed merchant information for KYC review
     * 
     * @param merchantId the merchant UUID
     * @return Merchant details including business info and bank details
     * 
     * GET /api/v1/admin/kyc/merchant/{merchantId}
     */
    @GetMapping("/kyc/merchant/{merchantId}")
    public ResponseEntity<ApiResponse<KycPendingMerchantResponse>> getMerchantForReview(
            @PathVariable UUID merchantId) {
        log.info("Admin fetching merchant details for review: {}", merchantId);
        KycPendingMerchantResponse merchant = adminKycService.getMerchantForReview(merchantId);
        return ResponseEntity.ok(ApiResponse.success("Merchant details retrieved", merchant));
    }

    /**
     * Approve merchant KYC (REST-ful path variable version)
     * Generates API key upon approval (shown only once)
     *
     * @param merchantId the merchant UUID from URL path
     * @param notes      optional admin notes
     * @param principal  authenticated admin (adminId extracted from JWT)
     * @return Review response with API key
     *
     * PUT /api/v1/admin/kyc/{merchantId}/approve
     * Authorization: Bearer {admin-jwt-token}
     */
    @PutMapping("/kyc/{merchantId}/approve")
    public ResponseEntity<ApiResponse<KycReviewResponse>> approveKyc(
            @PathVariable UUID merchantId,
            @RequestParam(required = false) String notes,
            Principal principal) {

        UUID adminId = UUID.fromString(principal.getName());
        log.info("Admin {} approving KYC for merchant: {}", adminId, merchantId);

        KycReviewRequest request = new KycReviewRequest();
        request.setMerchantId(merchantId);
        request.setAdminNotes(notes);

        KycReviewResponse response = adminKycService.approveKyc(request, adminId);
        return ResponseEntity.ok(ApiResponse.success("KYC approved successfully", response));
    }

    /**
     * Approve merchant KYC (legacy body version - kept for backward compatibility)
     */
    @PostMapping("/kyc/approve")
    public ResponseEntity<ApiResponse<KycReviewResponse>> approveKycLegacy(
            @Valid @RequestBody KycReviewRequest request,
            Principal principal) {

        UUID adminId = UUID.fromString(principal.getName());
        log.info("Admin {} approving KYC for merchant: {}", adminId, request.getMerchantId());
        KycReviewResponse response = adminKycService.approveKyc(request, adminId);
        return ResponseEntity.ok(ApiResponse.success("KYC approved successfully", response));
    }

    /**
     * Reject merchant KYC (REST-ful path variable version)
     *
     * @param merchantId      the merchant UUID from URL path
     * @param rejectionReason reason for rejection
     * @param notes           optional admin notes
     * @param principal       authenticated admin
     * @return Review response
     *
     * PUT /api/v1/admin/kyc/{merchantId}/reject
     */
    @PutMapping("/kyc/{merchantId}/reject")
    public ResponseEntity<ApiResponse<KycReviewResponse>> rejectKyc(
            @PathVariable UUID merchantId,
            @RequestParam String rejectionReason,
            @RequestParam(required = false) String notes,
            Principal principal) {

        UUID adminId = UUID.fromString(principal.getName());
        log.info("Admin {} rejecting KYC for merchant: {}", adminId, merchantId);

        KycReviewRequest request = new KycReviewRequest();
        request.setMerchantId(merchantId);
        request.setRejectionReason(rejectionReason);
        request.setAdminNotes(notes);

        KycReviewResponse response = adminKycService.rejectKyc(request, adminId);
        return ResponseEntity.ok(ApiResponse.success("KYC rejected", response));
    }

    /**
     * Reject merchant KYC (legacy body version - kept for backward compatibility)
     */
    @PostMapping("/kyc/reject")
    public ResponseEntity<ApiResponse<KycReviewResponse>> rejectKycLegacy(
            @Valid @RequestBody KycReviewRequest request,
            Principal principal) {

        UUID adminId = UUID.fromString(principal.getName());
        log.info("Admin {} rejecting KYC for merchant: {}", adminId, request.getMerchantId());
        KycReviewResponse response = adminKycService.rejectKyc(request, adminId);
        return ResponseEntity.ok(ApiResponse.success("KYC rejected", response));
    }

    /**
     * Put merchant KYC under manual review
     * Use this when additional verification is needed
     *
     * @param merchantId the merchant UUID
     * @param notes      review notes
     * @param principal  authenticated admin
     * @return Review response
     *
     * POST /api/v1/admin/kyc/under-review/{merchantId}
     */
    @PostMapping("/kyc/under-review/{merchantId}")
    public ResponseEntity<ApiResponse<KycReviewResponse>> putUnderReview(
            @PathVariable UUID merchantId,
            @RequestParam String notes,
            Principal principal) {

        UUID adminId = UUID.fromString(principal.getName());
        log.info("Admin {} putting merchant {} under review", adminId, merchantId);
        KycReviewResponse response = adminKycService.putUnderReview(merchantId, adminId, notes);
        return ResponseEntity.ok(ApiResponse.success("Merchant put under review", response));
    }

    /**
     * Regenerate API key for a merchant
     * Use this if merchant lost their API key or for security rotation
     *
     * @param merchantId the merchant UUID
     * @param principal  authenticated admin
     * @param reason     optional reason for regeneration
     * @return New API key (shown only once)
     *
     * POST /api/v1/admin/merchants/{merchantId}/regenerate-api-key
     */
    @PostMapping("/merchants/{merchantId}/regenerate-api-key")
    public ResponseEntity<ApiResponse<ApiKeyResponse>> regenerateApiKey(
            @PathVariable UUID merchantId,
            Principal principal,
            @RequestParam(required = false) String reason) {

        UUID adminId = UUID.fromString(principal.getName());
        log.info("Admin {} regenerating API key for merchant: {}", adminId, merchantId);
        ApiKeyResponse response = adminKycService.regenerateApiKey(merchantId, adminId, reason);
        return ResponseEntity.ok(ApiResponse.success(
                "API key regenerated successfully. New key shown below.",
                response));
    }
}
