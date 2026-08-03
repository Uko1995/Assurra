package com.uko.eaas.identity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

/**
 * Request DTO for KYC review (approve/reject)
 */
@Data
public class KycReviewRequest {
    
    @NotNull(message = "Merchant ID is required")
    private UUID merchantId;
    
    private String rejectionReason;
    
    private String adminNotes;
}
