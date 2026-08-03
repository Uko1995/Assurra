package com.uko.eaas.escrow.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "fee_configurations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeeConfiguration {

    @Id
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "merchant_id")
    private UUID merchantId;

    @Column(name = "fee_type", nullable = false, length = 20)
    private String feeType;

    @Column(name = "fee_value", nullable = false, precision = 5, scale = 4)
    private BigDecimal feeValue;

    @Column(name = "min_fee", precision = 15, scale = 2)
    private BigDecimal minFee = BigDecimal.valueOf(500);

    @Column(name = "max_fee", precision = 15, scale = 2)
    private BigDecimal maxFee = BigDecimal.valueOf(50000);

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "effective_from")
    private LocalDateTime effectiveFrom = LocalDateTime.now();

    @Column(name = "effective_until")
    private LocalDateTime effectiveUntil;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
