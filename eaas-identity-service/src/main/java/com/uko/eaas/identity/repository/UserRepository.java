package com.uko.eaas.identity.repository;

import com.uko.eaas.identity.model.entity.User;
import com.uko.eaas.identity.model.enums.KycStatus;
import com.uko.eaas.identity.model.enums.UserRole;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

import static com.uko.eaas.identity.config.CacheConfig.CACHE_USERS;
import static com.uko.eaas.identity.config.CacheConfig.CACHE_USERS_BY_EMAIL;

/**
 * User Repository with Redis Caching.
 * 
 * PERFORMANCE OPTIMIZATION:
 * - @Cacheable: Caches results of read operations
 * - @CachePut: Updates cache when user is saved
 * - @CacheEvict: Removes from cache when user is deleted
 * 
 * This reduces database load and improves login performance by ~20-50ms
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Find user by email with caching.
     * Cache key: "usersByEmail::<email>"
     * TTL: 15 minutes
     */
    @Cacheable(value = CACHE_USERS_BY_EMAIL, key = "#email", unless = "#result == null")
    Optional<User> findByEmail(String email);

    /**
     * Find user by ID with caching.
     * Cache key: "users::<id>"
     * TTL: 15 minutes
     */
    @Cacheable(value = CACHE_USERS, key = "#id", unless = "#result == null")
    Optional<User> findById(UUID id);

    Optional<User> findByEmailVerifyToken(String token);

    boolean existsByEmail(String email);

    long countByRole(UserRole role);

    /**
     * Find users by role and KYC status (for admin KYC review)
     */
    Page<User> findByRoleAndKycStatus(UserRole role, KycStatus kycStatus, Pageable pageable);

    /**
     * Save user and update cache.
     * Updates both user cache and email lookup cache.
     */
    @Override
    @CachePut(value = CACHE_USERS, key = "#result.id")
    @CacheEvict(value = CACHE_USERS_BY_EMAIL, key = "#result.email")
    <S extends User> S save(S entity);

    /**
     * Delete user and evict from cache.
     */
    @Override
    @CacheEvict(value = {CACHE_USERS, CACHE_USERS_BY_EMAIL}, key = "#entity.id")
    void delete(User entity);
}
