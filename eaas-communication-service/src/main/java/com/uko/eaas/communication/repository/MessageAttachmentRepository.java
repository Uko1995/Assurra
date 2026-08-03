package com.uko.eaas.communication.repository;

import com.uko.eaas.communication.model.entity.MessageAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MessageAttachmentRepository extends JpaRepository<MessageAttachment, MessageAttachment.MessageAttachmentId> {

    List<MessageAttachment> findByMessageIdOrderByAttachmentOrderAsc(UUID messageId);

    List<MessageAttachment> findByEvidenceId(UUID evidenceId);

    long countByMessageId(UUID messageId);

    void deleteByMessageId(UUID messageId);
}
