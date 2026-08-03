package com.uko.eaas.identity.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for merchants pending KYC review
 */
@Data
@Builder
public class KycPendingMerchantResponse {
    
    private UUID userId;
    private String email;
    private String fullName;
    private String phone;
    
    private String businessName;
    private String businessType;
    private String businessRegNumber;
    
    private String bankName;
    private String bankCode;
    private String bankAccountNumber;
    private String bvn;
    
    private LocalDateTime kycSubmittedAt;
    private LocalDateTime userCreatedAt;
    
    private Boolean emailVerified;
}
