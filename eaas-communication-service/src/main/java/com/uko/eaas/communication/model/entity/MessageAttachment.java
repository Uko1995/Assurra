package com.uko.eaas.communication.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "message_attachments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(MessageAttachment.MessageAttachmentId.class)
public class MessageAttachment {

    @Id
    @Column(name = "message_id", nullable = false)
    private UUID messageId;

    @Id
    @Column(name = "evidence_id", nullable = false)
    private UUID evidenceId;

    @Column(name = "attachment_order", nullable = false)
    private Integer attachmentOrder = 0;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MessageAttachmentId implements Serializable {
        private UUID messageId;
        private UUID evidenceId;
    }
}
