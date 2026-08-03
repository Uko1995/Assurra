package com.uko.eaas.identity.dto;

import com.uko.eaas.identity.model.enums.KycStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for merchant KYC submission.
 */
@Data
@Builder
public class KycSubmitResponse {
    private UUID userId;
    private String businessName;
    private KycStatus kycStatus;
    private LocalDateTime kycSubmittedAt;
    private String message;
}
