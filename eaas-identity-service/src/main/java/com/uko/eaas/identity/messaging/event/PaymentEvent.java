package com.uko.eaas.identity.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEvent implements Serializable {
    private String eventType;
    private String reference;
    private String escrowReference;
    private String customerId;
    private String merchantId;
    private BigDecimal amount;
    private BigDecimal fee;
    private String currency;
    private String status;
    private String channel;
    private String cardLast4;
    private String cardBrand;
    private String bankName;
    private BigDecimal refundAmount;
    private String refundReason;
    private LocalDateTime timestamp;
}
