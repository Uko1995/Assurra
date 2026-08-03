package com.uko.eaas.escrow.messaging.event;

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
public class EscrowEvent implements Serializable {
    private String eventType;
    private String escrowId;
    private String reference;
    private String customerId;
    private String merchantId;
    private BigDecimal amount;
    private BigDecimal merchantAmount;
    private BigDecimal escrowFee;
    private String currency;
    private String status;
    private String paymentReference;
    private String trackingNumber;
    private String logisticsProvider;
    private String triggeredBy;
    private LocalDateTime timestamp;
}
