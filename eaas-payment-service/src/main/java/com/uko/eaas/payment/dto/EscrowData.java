package com.uko.eaas.payment.dto;

import com.uko.eaas.payment.model.enums.EscrowStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class EscrowData {
    private String reference;
    private UUID customerId;
    private UUID merchantId;
    private EscrowStatus status;
    private BigDecimal amount;
    private BigDecimal escrowFee;
    private BigDecimal merchantAmount;
    private String currency;
}
