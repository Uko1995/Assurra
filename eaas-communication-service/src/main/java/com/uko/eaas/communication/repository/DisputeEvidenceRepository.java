package com.uko.eaas.communication.repository;

import com.uko.eaas.communication.model.entity.DisputeEvidence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface DisputeEvidenceRepository extends JpaRepository<DisputeEvidence, UUID> {

    List<DisputeEvidence> findByDisputeIdAndDeletedAtIsNull(UUID disputeId);

    List<DisputeEvidence> findByDisputeIdAndUploadedByAndDeletedAtIsNull(UUID disputeId, UUID uploadedBy);

    List<DisputeEvidence> findByUploadedBy(UUID uploadedBy);

    List<DisputeEvidence> findByExpiresAtBeforeAndDeletedAtIsNull(LocalDateTime expiresAt);

    long countByDisputeIdAndDeletedAtIsNull(UUID disputeId);
}
