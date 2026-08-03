package com.uko.eaas.escrow.model.entity;

import com.uko.eaas.escrow.model.enums.EscrowStatus;
import com.uko.eaas.escrow.model.enums.TriggeredBy;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "escrow_state_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EscrowStateHistory {

    @Id
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "escrow_id", nullable = false)
    private UUID escrowId;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status")
    private EscrowStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false)
    private EscrowStatus toStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TriggeredBy triggeredBy;

    @Column(name = "triggered_by_id")
    private UUID triggeredById;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(columnDefinition = "jsonb")
    private String metadata;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
