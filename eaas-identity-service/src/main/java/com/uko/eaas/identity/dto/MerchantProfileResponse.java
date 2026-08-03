package com.uko.eaas.identity.dto;

import com.uko.eaas.identity.model.enums.KycStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for merchant profile retrieval.
 */
@Data
@Builder
public class MerchantProfileResponse {
    // User info
    private UUID userId;
    private String email;
    private String fullName;
    private String phone;
    private KycStatus kycStatus;
    private Boolean emailVerified;
    private Boolean isActive;
    private LocalDateTime lastLoginAt;

    // Merchant profile info
    private String businessName;
    private String businessType;
    private String businessRegNumber;
    private String bankAccountNumber;
    private String bankCode;
    private String bankName;
    private String bvn;
    private String settlementEmail;
    private String apiKeyPrefix;
    private String webhookUrl;
    private Boolean isVerified;
    private LocalDateTime kycSubmittedAt;
    private LocalDateTime kycReviewedAt;
    private String kycRejectionReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
