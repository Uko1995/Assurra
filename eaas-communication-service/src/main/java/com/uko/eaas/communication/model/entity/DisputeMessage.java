package com.uko.eaas.communication.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "dispute_messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DisputeMessage {

    @Id
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "dispute_id", nullable = false)
    private UUID disputeId;

    @Column(name = "sender_id", nullable = false)
    private UUID senderId;

    @Column(name = "sender_type", nullable = false, length = 20)
    private String senderType; // CUSTOMER, MERCHANT, ADMIN, SYSTEM

    // Content
    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "is_internal")
    private Boolean isInternal = false;

    // Attachments (managed via MessageAttachment junction table)
    @Transient
    private List<UUID> attachmentIds;

    @Column(name = "has_attachments")
    private Boolean hasAttachments = false;

    // Status
    @Column(name = "read_by_customer")
    private Boolean readByCustomer = false;

    @Column(name = "read_by_merchant")
    private Boolean readByMerchant = false;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
