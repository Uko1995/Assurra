package com.uko.eaas.communication.model.entity;

import com.uko.eaas.communication.model.enums.DisputeReason;
import com.uko.eaas.communication.model.enums.DisputeStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "disputes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Dispute {

    @Id
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(unique = true, nullable = false, length = 100)
    private String reference;

    @Column(name = "escrow_reference", nullable = false, length = 100)
    private String escrowReference;

    // Parties
    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "merchant_id", nullable = false)
    private UUID merchantId;

    @Column(name = "raised_by", nullable = false)
    private UUID raisedBy;

    // Details
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DisputeReason reason;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "desired_outcome", columnDefinition = "TEXT")
    private String desiredOutcome;

    // Status
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DisputeStatus status = DisputeStatus.OPEN;

    // Amount at Dispute
    @Column(name = "amount_disputed", nullable = false, precision = 15, scale = 2)
    private BigDecimal amountDisputed;

    @Column(name = "resolution_amount", precision = 15, scale = 2)
    private BigDecimal resolutionAmount;

    // Resolution
    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    private String resolutionNotes;

    @Column(name = "resolved_by")
    private UUID resolvedBy;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    // Timeline
    @Column(name = "opened_at")
    private LocalDateTime openedAt = LocalDateTime.now();

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    // Communication
    @Column(name = "last_activity_at")
    private LocalDateTime lastActivityAt = LocalDateTime.now();

    @Column(name = "customer_notified")
    private Boolean customerNotified = false;

    @Column(name = "merchant_notified")
    private Boolean merchantNotified = false;

    // Metadata
    @Column(columnDefinition = "jsonb")
    private String metadata;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
