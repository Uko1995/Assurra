package com.uko.eaas.escrow.repository;

import com.uko.eaas.escrow.model.entity.EscrowTransaction;
import com.uko.eaas.escrow.model.enums.EscrowStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EscrowTransactionRepository extends JpaRepository<EscrowTransaction, UUID> {

    Optional<EscrowTransaction> findByReference(String reference);

    boolean existsByReference(String reference);

    boolean existsByIdempotencyKey(String idempotencyKey);

    Optional<EscrowTransaction> findByIdempotencyKey(String idempotencyKey);

    Page<EscrowTransaction> findByCustomerId(UUID customerId, Pageable pageable);

    Page<EscrowTransaction> findByMerchantId(UUID merchantId, Pageable pageable);

    Page<EscrowTransaction> findByStatus(EscrowStatus status, Pageable pageable);

    @Query("SELECT e FROM EscrowTransaction e WHERE e.status = :status AND e.autoReleaseAt <= :now")
    List<EscrowTransaction> findExpiredForAutoRelease(
            @Param("status") EscrowStatus status, 
            @Param("now") LocalDateTime now);

    @Query("SELECT e FROM EscrowTransaction e WHERE e.status = 'INITIATED' AND e.paymentExpiresAt <= :now")
    List<EscrowTransaction> findExpiredForCancellation(@Param("now") LocalDateTime now);

    long countByStatus(EscrowStatus status);

    long countByMerchantIdAndStatus(UUID merchantId, EscrowStatus status);
}
