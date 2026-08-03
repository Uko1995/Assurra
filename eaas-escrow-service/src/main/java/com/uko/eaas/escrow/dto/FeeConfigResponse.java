package com.uko.eaas.escrow.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class FeeConfigResponse {

    private UUID id;
    private UUID merchantId;
    private String feeType;
    private BigDecimal feeValue;
    private BigDecimal minFee;
    private BigDecimal maxFee;
    private Boolean isActive;
    private LocalDateTime effectiveFrom;
    private LocalDateTime effectiveUntil;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
