package com.uko.eaas.escrow.dto;

import com.uko.eaas.escrow.model.enums.EscrowStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class EscrowResponse {

    private UUID id;
    private String reference;
    private UUID customerId;
    private UUID merchantId;

    // Amounts
    private BigDecimal amount;
    private BigDecimal escrowFee;
    private BigDecimal merchantAmount;
    private String currency;

    // Status
    private EscrowStatus status;

    // Product Details
    private String productDescription;
    private Integer productQuantity;
    private Integer agreedDeliveryDays;

    // Payment
    private String paymentReference;
    private String paymentChannel;
    private String paymentLink;
    private LocalDateTime fundedAt;
    private LocalDateTime paymentExpiresAt;

    // Shipping
    private String trackingNumber;
    private String logisticsProvider;
    private LocalDate estimatedDeliveryDate;
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;

    // Confirmation Window
    private LocalDateTime confirmationDeadline;
    private LocalDateTime autoReleaseAt;
    private LocalDateTime confirmedAt;

    // Payout
    private String payoutReference;
    private LocalDateTime paidOutAt;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
