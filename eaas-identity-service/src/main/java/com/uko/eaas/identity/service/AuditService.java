package com.uko.eaas.identity.service;

import com.uko.eaas.identity.model.entity.AuditLog;
import com.uko.eaas.identity.model.enums.UserRole;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface AuditService {

    void logUserRegistered(UUID userId, String email, UserRole role);

    void logUserLoggedIn(UUID userId, String email, UserRole role);

    void logUserLoggedIn(UUID userId, String email, UserRole role, String ipAddress, String userAgent);

    /**
     * Log a failed login attempt.
     *
     * @param email the email that was attempted (may not exist in system)
     * @param ipAddress the IP address of the request
     * @param userAgent the user agent string
     * @param reason the reason for failure (e.g., "INVALID_PASSWORD", "USER_NOT_FOUND", "ACCOUNT_LOCKED")
     */
    void logLoginFailed(String email, String ipAddress, String userAgent, String reason);

    void logEmailVerified(UUID userId, String email, UserRole role);

    void logKycSubmitted(UUID merchantId, String businessName);

    void logKycApproved(UUID merchantId, String businessName, UUID approvedBy);

    void logApiKeyGenerated(UUID merchantId, String apiKeyPrefix, UserRole generatedByRole);

    /**
     * Generic action logging method
     *
     * @param action the action performed
     * @param entityId the entity ID affected
     * @param performedBy the user who performed the action
     * @param performedByRole the role of the user who performed the action
     * @param details additional details about the action
     */
    void logAction(String action, UUID entityId, UUID performedBy, UserRole performedByRole, String details);

    // ============ ASYNC METHODS FOR NON-BLOCKING OPERATIONS ============

    /**
     * Asynchronously log user login.
     * Use this in performance-critical paths like login to avoid blocking the response.
     */
    CompletableFuture<Void> logUserLoggedInAsync(UUID userId, String email, UserRole role);

    /**
     * Asynchronously log user login with request context (IP, user-agent).
     * Use this for enhanced security auditing.
     */
    CompletableFuture<Void> logUserLoggedInAsync(UUID userId, String email, UserRole role, String ipAddress, String userAgent);

    /**
     * Asynchronously log a failed login attempt.
     * Use this to track brute-force attacks without blocking the response.
     */
    CompletableFuture<Void> logLoginFailedAsync(String email, String ipAddress, String userAgent, String reason);

    /**
     * Asynchronously log user registration.
     */
    CompletableFuture<Void> logUserRegisteredAsync(UUID userId, String email, UserRole role);

    /**
     * Asynchronously log email verification.
     */
    CompletableFuture<Void> logEmailVerifiedAsync(UUID userId, String email, UserRole role);

    /**
     * Asynchronously log KYC submission.
     */
    CompletableFuture<Void> logKycSubmittedAsync(UUID merchantId, String businessName);

    /**
     * Asynchronously log KYC approval.
     */
    CompletableFuture<Void> logKycApprovedAsync(UUID merchantId, String businessName, UUID approvedBy);

    /**
     * Asynchronously log API key generation.
     */
    CompletableFuture<Void> logApiKeyGeneratedAsync(UUID merchantId, String apiKeyPrefix, UserRole generatedByRole);

    /**
     * Generic async action logging method.
     */
    CompletableFuture<Void> logActionAsync(String action, UUID entityId, UUID performedBy, UserRole performedByRole, String details);
}
