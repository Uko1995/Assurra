package com.uko.eaas.escrow.repository;

import com.uko.eaas.escrow.model.entity.FeeConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FeeConfigurationRepository extends JpaRepository<FeeConfiguration, UUID> {

    @Query("SELECT f FROM FeeConfiguration f WHERE f.merchantId = :merchantId AND f.isActive = true " +
           "AND f.effectiveFrom <= CURRENT_TIMESTAMP AND (f.effectiveUntil IS NULL OR f.effectiveUntil > CURRENT_TIMESTAMP) " +
           "ORDER BY f.effectiveFrom DESC")
    Optional<FeeConfiguration> findActiveByMerchantId(@Param("merchantId") UUID merchantId);

    @Query("SELECT f FROM FeeConfiguration f WHERE f.merchantId IS NULL AND f.isActive = true " +
           "AND f.effectiveFrom <= CURRENT_TIMESTAMP AND (f.effectiveUntil IS NULL OR f.effectiveUntil > CURRENT_TIMESTAMP) " +
           "ORDER BY f.effectiveFrom DESC")
    Optional<FeeConfiguration> findGlobalDefault();
}
