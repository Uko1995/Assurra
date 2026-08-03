package com.uko.eaas.communication.messaging.event;

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
public class PayoutEvent implements Serializable {
    private String eventType;
    private String reference;
    private String escrowReference;
    private String merchantId;
    private BigDecimal amount;
    private BigDecimal fee;
    private BigDecimal netAmount;
    private String currency;
    private String status;
    private String bankCode;
    private String bankName;
    private String accountNumber;
    private String accountName;
    private LocalDateTime timestamp;
}
