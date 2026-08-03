package com.uko.eaas.payment.dto;

import com.uko.eaas.payment.model.enums.PayoutMethod;
import com.uko.eaas.payment.model.enums.PayoutStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class PayoutResponse {

    private UUID id;
    private String reference;
    private String escrowReference;
    private UUID merchantId;

    private BigDecimal amount;
    private BigDecimal fee;
    private BigDecimal netAmount;
    private String currency;

    private PayoutStatus status;
    private PayoutMethod method;

    private String bankName;
    private String accountNumber;
    private String accountName;

    private LocalDateTime scheduledAt;
    private LocalDateTime processedAt;
    private LocalDateTime completedAt;

    private LocalDateTime createdAt;
}
