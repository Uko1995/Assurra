package com.uko.eaas.identity.dto;

import com.uko.eaas.identity.model.enums.KycStatus;
import com.uko.eaas.identity.model.enums.UserRole;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class UserResponse {
    private UUID id;
    private String email;
    private String phone;
    private String fullName;
    private UserRole role;
    private KycStatus kycStatus;
    private Boolean emailVerified;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
    
    // Merchant-specific fields
    private String businessName;
    private String businessType;
    private Boolean isVerified;
    private String apiKeyPrefix;
}
