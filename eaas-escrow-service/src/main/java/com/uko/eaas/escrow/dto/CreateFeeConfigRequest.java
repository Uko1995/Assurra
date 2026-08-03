package com.uko.eaas.escrow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CreateFeeConfigRequest {

    private UUID merchantId;

    @NotBlank
    private String feeType;

    @NotNull
    private BigDecimal feeValue;

    private BigDecimal minFee = BigDecimal.valueOf(500);

    private BigDecimal maxFee = BigDecimal.valueOf(50000);

    private LocalDateTime effectiveFrom = LocalDateTime.now();

    private LocalDateTime effectiveUntil;
}
