package com.uko.eaas.escrow.model.entity;

import com.uko.eaas.escrow.model.enums.EscrowStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "escrow_transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EscrowTransaction {

    @Id
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(unique = true, nullable = false, length = 100)
    private String reference;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "merchant_id", nullable = false)
    private UUID merchantId;

    @Column(name = "merchant_profile_id", nullable = false)
    private UUID merchantProfileId;

    // Amounts
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "escrow_fee", nullable = false, precision = 15, scale = 2)
    private BigDecimal escrowFee;

    @Column(name = "merchant_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal merchantAmount;

    @Column(length = 3)
    private String currency = "NGN";

    // Status
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EscrowStatus status = EscrowStatus.INITIATED;

    // Product Details
    @Column(name = "product_description", nullable = false, columnDefinition = "TEXT")
    private String productDescription;

    @Column(name = "product_quantity")
    private Integer productQuantity = 1;

    @Column(name = "agreed_delivery_days")
    private Integer agreedDeliveryDays = 7;

    // Payment
    @Column(name = "payment_reference", length = 255)
    private String paymentReference;

    @Column(name = "payment_channel", length = 50)
    private String paymentChannel;

    @Column(name = "payment_link", length = 500)
    private String paymentLink;

    @Column(name = "funded_at")
    private LocalDateTime fundedAt;

    @Column(name = "payment_expires_at")
    private LocalDateTime paymentExpiresAt;

    // Shipping
    @Column(name = "tracking_number", length = 255)
    private String trackingNumber;

    @Column(name = "logistics_provider", length = 100)
    private String logisticsProvider;

    @Column(name = "estimated_delivery_date")
    private LocalDate estimatedDeliveryDate;

    @Column(name = "shipped_at")
    private LocalDateTime shippedAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    // Confirmation Window
    @Column(name = "confirmation_deadline")
    private LocalDateTime confirmationDeadline;

    @Column(name = "auto_release_at")
    private LocalDateTime autoReleaseAt;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    // Payout
    @Column(name = "payout_reference", length = 255)
    private String payoutReference;

    @Column(name = "paid_out_at")
    private LocalDateTime paidOutAt;

    // Idempotency
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
