package com.uko.eaas.escrow.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class FeeBreakdown {
    private BigDecimal amount;
    private BigDecimal escrowFee;
    private BigDecimal merchantAmount;
}
