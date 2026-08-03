package com.uko.eaas.communication.repository;

import com.uko.eaas.communication.model.entity.Dispute;
import com.uko.eaas.communication.model.enums.DisputeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DisputeRepository extends JpaRepository<Dispute, UUID> {

    Optional<Dispute> findByReference(String reference);

    Optional<Dispute> findByEscrowReference(String escrowReference);

    Page<Dispute> findByCustomerIdOrderByOpenedAtDesc(UUID customerId, Pageable pageable);

    Page<Dispute> findByMerchantIdOrderByOpenedAtDesc(UUID merchantId, Pageable pageable);

    Page<Dispute> findByStatusOrderByOpenedAtDesc(DisputeStatus status, Pageable pageable);

    @Query("SELECT d FROM Dispute d WHERE d.customerId = :userId OR d.merchantId = :userId ORDER BY d.lastActivityAt DESC")
    Page<Dispute> findByUserId(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT d FROM Dispute d WHERE d.status IN ('OPEN', 'UNDER_REVIEW') ORDER BY d.lastActivityAt DESC")
    Page<Dispute> findActiveDisputes(Pageable pageable);

    boolean existsByEscrowReference(String escrowReference);

    long countByStatus(DisputeStatus status);

    @Query("SELECT COUNT(d) FROM Dispute d WHERE d.status IN ('OPEN', 'UNDER_REVIEW')")
    long countActiveDisputes();
}
