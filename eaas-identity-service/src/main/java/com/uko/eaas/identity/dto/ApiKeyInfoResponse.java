package com.uko.eaas.identity.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for merchant API key information.
 * Only returns the prefix and metadata — never the full key.
 */
@Data
@Builder
public class ApiKeyInfoResponse {
    private UUID merchantId;
    private String businessName;
    private String apiKeyPrefix;
    private String apiKeyIdentifier;
    private Boolean isVerified;
    private LocalDateTime verifiedAt;
}
