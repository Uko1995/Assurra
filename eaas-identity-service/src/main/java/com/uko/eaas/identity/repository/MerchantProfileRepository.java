package com.uko.eaas.identity.repository;

import com.uko.eaas.identity.model.entity.MerchantProfile;
import com.uko.eaas.identity.model.enums.KycStatus;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

import static com.uko.eaas.identity.config.CacheConfig.CACHE_MERCHANT_PROFILES;

/**
 * Merchant Profile Repository with Redis Caching.
 * 
 * PERFORMANCE OPTIMIZATION:
 * - Caches merchant profile lookups by userId (called during login for merchants)
 * - This reduces login latency for merchant users by ~20-30ms
 */
@Repository
public interface MerchantProfileRepository extends JpaRepository<MerchantProfile, UUID> {

    /**
     * Find merchant profile by user ID with caching.
     * Cache key: "merchantProfiles::<userId>"
     * TTL: 10 minutes
     */
    @Cacheable(value = CACHE_MERCHANT_PROFILES, key = "#userId", unless = "#result == null")
    @EntityGraph(attributePaths = "user")
    Optional<MerchantProfile> findByUserId(UUID userId);

    Optional<MerchantProfile> findByApiKeyIdentifier(String apiKeyIdentifier);

    Optional<MerchantProfile> findByApiKeyPrefix(String apiKeyPrefix);

    boolean existsByBusinessName(String businessName);

    Page<MerchantProfile> findByUserKycStatus(KycStatus kycStatus, Pageable pageable);

    long countByUserKycStatus(KycStatus kycStatus);

    /**
     * Save merchant profile and update cache.
     */
    @Override
    @CachePut(value = CACHE_MERCHANT_PROFILES, key = "#result.user.id")
    <S extends MerchantProfile> S save(S entity);

    /**
     * Delete merchant profile and evict from cache.
     */
    @Override
    @CacheEvict(value = CACHE_MERCHANT_PROFILES, key = "#entity.user.id")
    void delete(MerchantProfile entity);
}
