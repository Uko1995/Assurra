package com.uko.eaas.identity.dto;

import com.uko.eaas.identity.model.enums.KycStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for KYC review operations
 */
@Data
@Builder
public class KycReviewResponse {
    
    private UUID merchantId;
    private String businessName;
    private KycStatus kycStatus;
    private String message;
    private String apiKey;
    private String apiKeyPrefix;
    private LocalDateTime reviewedAt;
    private UUID reviewedBy;
    private String rejectionReason;
    private String adminNotes;
}
