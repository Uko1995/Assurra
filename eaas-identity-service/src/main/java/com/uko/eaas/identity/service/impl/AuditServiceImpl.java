package com.uko.eaas.identity.service.impl;

import com.uko.eaas.identity.model.entity.AuditLog;
import com.uko.eaas.identity.model.enums.UserRole;
import com.uko.eaas.identity.repository.AuditLogRepository;
import com.uko.eaas.identity.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Audit Service Implementation.
 * 
 * PERFORMANCE OPTIMIZATION:
 * - Provides both SYNC and ASYNC methods for audit logging
 * - Use ASYNC methods in performance-critical paths (login, registration) to avoid blocking
 * - Async methods run in a separate thread pool and don't block the main request thread
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;

    // ============ SYNC METHODS (Use for non-critical paths) ============

    @Override
    @Transactional
    public void logUserRegistered(UUID userId, String email, UserRole role) {
        AuditLog auditLog = AuditLog.builder()
                .entityType("USER")
                .entityId(userId)
                .action("USER_REGISTERED")
                .performedBy(userId)
                .performedByRole(role)
                .metadata(String.format("{\"email\": \"%s\", \"role\": \"%s\"}", email, role))
                .build();

        auditLogRepository.save(auditLog);
        log.info("Audit: {} registered - id={}, email={}", role, userId, email);
    }

    @Override
    @Transactional
    public void logUserLoggedIn(UUID userId, String email, UserRole role) {
        AuditLog auditLog = AuditLog.builder()
                .entityType("USER")
                .entityId(userId)
                .action("USER_LOGGED_IN")
                .performedBy(userId)
                .performedByRole(role)
                .metadata(String.format("{\"email\": \"%s\"}", email))
                .build();

        auditLogRepository.save(auditLog);
        log.info("Audit: {} logged in - id={}, email={}", role, userId, email);
    }

    @Override
    @Transactional
    public void logUserLoggedIn(UUID userId, String email, UserRole role, String ipAddress, String userAgent) {
        AuditLog auditLog = AuditLog.builder()
                .entityType("USER")
                .entityId(userId)
                .action("USER_LOGGED_IN")
                .performedBy(userId)
                .performedByRole(role)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .metadata(String.format("{\"email\": \"%s\", \"ip\": \"%s\", \"success\": true}", email, ipAddress))
                .build();

        auditLogRepository.save(auditLog);
        log.info("Audit: {} logged in - id={}, email={}, ip={}", role, userId, email, ipAddress);
    }

    @Override
    @Transactional
    public void logLoginFailed(String email, String ipAddress, String userAgent, String reason) {
        // SECURITY: Don't create entity for failed logins on non-existent users
        // Use a null UUID for entityId to indicate failed attempt
        AuditLog auditLog = AuditLog.builder()
                .entityType("USER")
                .entityId(new UUID(0, 0))  // Null UUID for failed attempts
                .action("LOGIN_FAILED")
                .performedBy(null)
                .performedByRole(null)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .metadata(String.format("{\"email\": \"%s\", \"reason\": \"%s\", \"ip\": \"%s\", \"success\": false}",
                        email, reason, ipAddress))
                .build();

        auditLogRepository.save(auditLog);
        log.warn("Security Audit: Failed login attempt - email={}, reason={}, ip={}", email, reason, ipAddress);
    }

    @Override
    @Transactional
    public void logEmailVerified(UUID userId, String email, UserRole role) {
        AuditLog auditLog = AuditLog.builder()
                .entityType("USER")
                .entityId(userId)
                .action("EMAIL_VERIFIED")
                .performedBy(userId)
                .performedByRole(role)
                .metadata(String.format("{\"email\": \"%s\"}", email))
                .build();

        auditLogRepository.save(auditLog);
        log.info("Audit: {} email verified - id={}, email={}", role, userId, email);
    }

    @Override
    @Transactional
    public void logKycSubmitted(UUID merchantId, String businessName) {
        AuditLog auditLog = AuditLog.builder()
                .entityType("MERCHANT")
                .entityId(merchantId)
                .action("KYC_SUBMITTED")
                .performedBy(merchantId)
                .performedByRole(UserRole.MERCHANT)
                .metadata(String.format("{\"businessName\": \"%s\"}", businessName))
                .build();

        auditLogRepository.save(auditLog);
        log.info("Audit: KYC submitted - merchantId={}, businessName={}", merchantId, businessName);
    }

    @Override
    @Transactional
    public void logKycApproved(UUID merchantId, String businessName, UUID approvedBy) {
        AuditLog auditLog = AuditLog.builder()
                .entityType("MERCHANT")
                .entityId(merchantId)
                .action("KYC_APPROVED")
                .performedBy(approvedBy)
                .performedByRole(UserRole.ADMIN)
                .metadata(String.format("{\"businessName\": \"%s\"}", businessName))
                .build();

        auditLogRepository.save(auditLog);
        log.info("Audit: KYC approved by ADMIN - merchantId={}, businessName={}, approvedBy={}",
                merchantId, businessName, approvedBy);
    }

    @Override
    @Transactional
    public void logApiKeyGenerated(UUID merchantId, String apiKeyPrefix, UserRole generatedByRole) {
        AuditLog auditLog = AuditLog.builder()
                .entityType("MERCHANT")
                .entityId(merchantId)
                .action("API_KEY_GENERATED")
                .performedBy(merchantId)
                .performedByRole(generatedByRole != null ? generatedByRole : UserRole.MERCHANT)
                .metadata(String.format("{\"apiKeyPrefix\": \"%s\"}", apiKeyPrefix))
                .build();

        auditLogRepository.save(auditLog);
        log.info("Audit: API key generated by {} - merchantId={}, prefix={}",
                generatedByRole != null ? generatedByRole : "MERCHANT", merchantId, apiKeyPrefix);
    }

    @Override
    @Transactional
    public void logAction(String action, UUID entityId, UUID performedBy, UserRole performedByRole, String details) {
        AuditLog auditLog = AuditLog.builder()
                .entityType("MERCHANT")
                .entityId(entityId)
                .action(action)
                .performedBy(performedBy)
                .performedByRole(performedByRole != null ? performedByRole : UserRole.ADMIN)
                .metadata(String.format("{\"details\": \"%s\"}", details != null ? details : ""))
                .build();

        auditLogRepository.save(auditLog);
        log.info("Audit: {} by {} - entityId={}, performedBy={}, details={}",
                action, performedByRole != null ? performedByRole : "ADMIN", entityId, performedBy, details);
    }

    // ============ ASYNC METHODS (Use for performance-critical paths) ============

    /**
     * Async audit logging - doesn't block the calling thread.
     * Uses REQUIRES_NEW transaction to ensure audit logs are saved even if main transaction fails.
     */
    @Override
    @Async("auditTaskExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CompletableFuture<Void> logUserLoggedInAsync(UUID userId, String email, UserRole role) {
        try {
            logUserLoggedIn(userId, email, role);
        } catch (Exception e) {
            // Log error but don't fail the main operation
            log.error("Failed to log login audit asynchronously: userId={}, error={}", userId, e.getMessage());
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    @Async("auditTaskExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CompletableFuture<Void> logUserLoggedInAsync(UUID userId, String email, UserRole role, String ipAddress, String userAgent) {
        try {
            logUserLoggedIn(userId, email, role, ipAddress, userAgent);
        } catch (Exception e) {
            log.error("Failed to login audit with context asynchronously: userId={}, ip={}, error={}", userId, ipAddress, e.getMessage());
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    @Async("auditTaskExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CompletableFuture<Void> logLoginFailedAsync(String email, String ipAddress, String userAgent, String reason) {
        try {
            logLoginFailed(email, ipAddress, userAgent, reason);
        } catch (Exception e) {
            log.error("Failed to log failed login audit asynchronously: email={}, ip={}, error={}", email, ipAddress, e.getMessage());
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    @Async("auditTaskExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CompletableFuture<Void> logUserRegisteredAsync(UUID userId, String email, UserRole role) {
        try {
            logUserRegistered(userId, email, role);
        } catch (Exception e) {
            log.error("Failed to log registration audit asynchronously: userId={}, error={}", userId, e.getMessage());
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    @Async("auditTaskExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CompletableFuture<Void> logEmailVerifiedAsync(UUID userId, String email, UserRole role) {
        try {
            logEmailVerified(userId, email, role);
        } catch (Exception e) {
            log.error("Failed to log email verification audit asynchronously: userId={}, error={}", userId, e.getMessage());
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    @Async("auditTaskExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CompletableFuture<Void> logKycSubmittedAsync(UUID merchantId, String businessName) {
        try {
            logKycSubmitted(merchantId, businessName);
        } catch (Exception e) {
            log.error("Failed to log KYC submission audit asynchronously: merchantId={}, error={}", merchantId, e.getMessage());
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    @Async("auditTaskExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CompletableFuture<Void> logKycApprovedAsync(UUID merchantId, String businessName, UUID approvedBy) {
        try {
            logKycApproved(merchantId, businessName, approvedBy);
        } catch (Exception e) {
            log.error("Failed to log KYC approval audit asynchronously: merchantId={}, error={}", merchantId, e.getMessage());
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    @Async("auditTaskExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CompletableFuture<Void> logApiKeyGeneratedAsync(UUID merchantId, String apiKeyPrefix, UserRole generatedByRole) {
        try {
            logApiKeyGenerated(merchantId, apiKeyPrefix, generatedByRole);
        } catch (Exception e) {
            log.error("Failed to log API key generation audit asynchronously: merchantId={}, error={}", merchantId, e.getMessage());
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    @Async("auditTaskExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CompletableFuture<Void> logActionAsync(String action, UUID entityId, UUID performedBy, UserRole performedByRole, String details) {
        try {
            logAction(action, entityId, performedBy, performedByRole, details);
        } catch (Exception e) {
            log.error("Failed to log action audit asynchronously: action={}, entityId={}, error={}", action, entityId, e.getMessage());
        }
        return CompletableFuture.completedFuture(null);
    }
}
