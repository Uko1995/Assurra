package com.uko.eaas.identity.dto;

import com.uko.eaas.identity.model.enums.KycStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class MerchantResponse {
    private UUID userId;
    private String businessName;
    private KycStatus kycStatus;
    private String apiKey;
    private String apiKeyPrefix;
    private LocalDateTime verifiedAt;
}
