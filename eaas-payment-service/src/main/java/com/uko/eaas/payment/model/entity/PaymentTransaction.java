package com.uko.eaas.payment.model.entity;

import com.uko.eaas.payment.model.enums.PaymentChannel;
import com.uko.eaas.payment.model.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payment_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentTransaction {

    @Id
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(unique = true, nullable = false, length = 100)
    private String reference;

    @Column(name = "escrow_reference", nullable = false, length = 100)
    private String escrowReference;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "merchant_id", nullable = false)
    private UUID merchantId;

    // Amounts
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Builder.Default
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal fee = BigDecimal.ZERO;

    @Column(length = 3)
    private String currency = "NGN";

    // Status
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Enumerated(EnumType.STRING)
    private PaymentChannel channel;

    // Interswitch Integration
    @Column(name = "interswitch_ref", length = 255)
    private String interswitchRef;

    @Column(name = "interswitch_auth", length = 255)
    private String interswitchAuth;

    // Payment Details
    @Column(name = "payment_link", length = 500)
    private String paymentLink;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "failed_at")
    private LocalDateTime failedAt;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    // Card Details (masked)
    @Column(name = "card_last4", length = 4)
    private String cardLast4;

    @Column(name = "card_brand", length = 20)
    private String cardBrand;

    // Bank Details
    @Column(name = "bank_name", length = 100)
    private String bankName;

    @Column(name = "account_number", length = 10)
    private String accountNumber;

    // Refund Details
    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;

    @Column(name = "refund_amount", precision = 15, scale = 2)
    private BigDecimal refundAmount;

    @Column(name = "refund_reason", columnDefinition = "TEXT")
    private String refundReason;

    @Column(name = "interswitch_refund_ref", length = 255)
    private String interswitchRefundRef;

    // Metadata
    @Column(name = "idempotency_key", unique = true, length = 255)
    private String idempotencyKey;

    @Column(columnDefinition = "jsonb")
    private String metadata;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
