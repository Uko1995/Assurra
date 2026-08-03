package com.uko.eaas.communication.repository;

import com.uko.eaas.communication.model.entity.DisputeMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DisputeMessageRepository extends JpaRepository<DisputeMessage, UUID> {

    Page<DisputeMessage> findByDisputeIdAndIsInternalFalseOrderByCreatedAtDesc(UUID disputeId, Pageable pageable);

    Page<DisputeMessage> findByDisputeIdOrderByCreatedAtDesc(UUID disputeId, Pageable pageable);

    List<DisputeMessage> findByDisputeIdAndIsInternalFalseAndReadByCustomerFalse(UUID disputeId);

    List<DisputeMessage> findByDisputeIdAndIsInternalFalseAndReadByMerchantFalse(UUID disputeId);

    @Query("SELECT COUNT(m) FROM DisputeMessage m WHERE m.disputeId = :disputeId AND m.isInternal = false")
    long countByDisputeId(@Param("disputeId") UUID disputeId);

    @Query("SELECT m FROM DisputeMessage m WHERE m.disputeId = :disputeId AND m.isInternal = false ORDER BY m.createdAt DESC")
    List<DisputeMessage> findRecentMessages(@Param("disputeId") UUID disputeId, Pageable pageable);
}
