package com.uko.eaas.payment.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class InitializePaymentResponse {

    private String reference;
    private String escrowReference;
    private String paymentLink;
    private BigDecimal amount;
    private String currency;
    private String status;
    private boolean replayed;
}
