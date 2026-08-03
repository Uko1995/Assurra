package com.uko.eaas.payment.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentWebhookPayload {

    private String event;
    private String reference;
    private String interswitchRef;
    private String PaymentReference;
    private String status;
    private BigDecimal amount;
    private String currency;
    private String channel;
    private String cardLast4;
    private String cardBrand;
    private String bankName;
    private LocalDateTime paidAt;
}
