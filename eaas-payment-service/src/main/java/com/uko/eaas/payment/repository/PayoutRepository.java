package com.uko.eaas.payment.repository;

import com.uko.eaas.payment.model.entity.Payout;
import com.uko.eaas.payment.model.enums.PayoutStatus;
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
public interface PayoutRepository extends JpaRepository<Payout, UUID> {

    Optional<Payout> findByReference(String reference);

    Optional<Payout> findByEscrowReference(String escrowReference);

    Optional<Payout> findByInterswitchRef(String interswitchRef);

    Page<Payout> findByMerchantId(UUID merchantId, Pageable pageable);

    Page<Payout> findByStatus(PayoutStatus status, Pageable pageable);

    List<Payout> findByStatusAndScheduledAtBefore(PayoutStatus status, LocalDateTime scheduledAt);

    List<Payout> findByStatusAndNextRetryAtBefore(PayoutStatus status, LocalDateTime nextRetryAt);

    @Query("SELECT p FROM Payout p WHERE p.status IN ('PENDING', 'QUEUED') AND p.scheduledAt <= :now")
    List<Payout> findPendingForProcessing(@Param("now") LocalDateTime now);

    @Query("SELECT p FROM Payout p WHERE p.status = 'FAILED' AND p.nextRetryAt <= :now AND p.retryCount < :maxRetries")
    List<Payout> findFailedForRetry(@Param("now") LocalDateTime now, @Param("maxRetries") int maxRetries);

    boolean existsByEscrowReference(String escrowReference);

    @Query("SELECT SUM(p.netAmount) FROM Payout p WHERE p.status = 'COMPLETED' AND p.merchantId = :merchantId")
    Double sumCompletedPayoutsByMerchant(@Param("merchantId") UUID merchantId);
}
