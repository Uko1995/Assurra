package com.uko.eaas.escrow.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentWebhookRequest {

    private String reference;
    private String paymentReference;
    private String status;
    private BigDecimal amount;
    private String channel;
    private LocalDateTime paidAt;
}
