package com.uko.eaas.payment.repository;

import com.uko.eaas.payment.model.entity.PaymentTransaction;
import com.uko.eaas.payment.model.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, UUID> {

    Optional<PaymentTransaction> findByReference(String reference);

    Optional<PaymentTransaction> findByEscrowReference(String escrowReference);

    Optional<PaymentTransaction> findByInterswitchRef(String interswitchRef);

    Optional<PaymentTransaction> findByIdempotencyKey(String idempotencyKey);

    Page<PaymentTransaction> findByCustomerId(UUID customerId, Pageable pageable);

    Page<PaymentTransaction> findByMerchantId(UUID merchantId, Pageable pageable);

    Page<PaymentTransaction> findByStatus(PaymentStatus status, Pageable pageable);

    boolean existsByReference(String reference);

    boolean existsByIdempotencyKey(String idempotencyKey);

    @Query("SELECT COUNT(p) FROM PaymentTransaction p WHERE p.status = :status")
    long countByStatus(@Param("status") PaymentStatus status);

    @Query("SELECT SUM(p.amount) FROM PaymentTransaction p WHERE p.status = 'SUCCESS' AND p.merchantId = :merchantId")
    Double sumSuccessfulPaymentsByMerchant(@Param("merchantId") UUID merchantId);

    // AML monitoring queries
    List<PaymentTransaction> findByCustomerIdAndAmountGreaterThanEqualAndCreatedAtAfter(
            UUID customerId, BigDecimal amount, LocalDateTime since);

    List<PaymentTransaction> findByCustomerIdAndAmountBetweenAndCreatedAtAfter(
            UUID customerId, BigDecimal minAmount, BigDecimal maxAmount, LocalDateTime since);
}
