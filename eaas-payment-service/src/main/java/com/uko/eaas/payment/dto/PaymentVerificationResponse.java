package com.uko.eaas.payment.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PaymentVerificationResponse {

    private String reference;
    private String status;
    private BigDecimal amount;
    private String channel;
    private String cardLast4;
    private String cardBrand;
    private String bankName;
    private LocalDateTime paidAt;
}
