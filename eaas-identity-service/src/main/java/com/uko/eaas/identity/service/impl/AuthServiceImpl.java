package com.uko.eaas.identity.service.impl;

import com.uko.eaas.identity.config.SecurityProperties;
import com.uko.eaas.identity.dto.*;
import com.uko.eaas.identity.exception.ConflictException;
import com.uko.eaas.identity.exception.ForbiddenException;
import com.uko.eaas.identity.exception.NotFoundException;
import com.uko.eaas.identity.exception.UnauthorizedException;
import com.uko.eaas.identity.exception.ValidationException;
import com.uko.eaas.identity.model.entity.KycSubmission;
import com.uko.eaas.identity.model.entity.MerchantProfile;
import com.uko.eaas.identity.model.entity.User;
import com.uko.eaas.identity.model.enums.KycStatus;
import com.uko.eaas.identity.model.enums.UserRole;
import com.uko.eaas.identity.repository.KycSubmissionRepository;
import com.uko.eaas.identity.repository.MerchantProfileRepository;
import com.uko.eaas.identity.repository.UserRepository;
import com.uko.eaas.identity.security.JwtUtil;
import com.uko.eaas.identity.service.AuthService;
import com.uko.eaas.identity.service.AuditService;
import com.uko.eaas.identity.service.EmailService;
import com.uko.eaas.identity.service.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final MerchantProfileRepository merchantProfileRepository;
    private final KycSubmissionRepository kycSubmissionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuditService auditService;
    private final EmailService emailService;
    private final SecurityProperties securityProperties;
    private final com.uko.eaas.identity.service.RefreshTokenService refreshTokenService;

    @Value("${api.key.prefix:sk_live_}")
    private String apiKeyPrefix;

    // Request context for audit logging
    private static final String UNKNOWN_IP = "unknown";
    private static final String UNKNOWN_USER_AGENT = "unknown";

    @Override
    @Transactional
    public UserResponse registerCustomer(CustomerRegisterRequest request) {
        log.info("Processing customer registration for email: {}", request.getEmail());
        
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email already registered: " + request.getEmail());
        }

        // Generate email verification token
        String verifyToken = generateVerificationToken();

        // Validate consent
        validateConsent(request.getTermsAccepted(), request.getDataProcessingConsent());

        // Create customer user
        User user = User.builder()
                .email(request.getEmail().toLowerCase().trim())
                .phone(request.getPhone())
                .fullName(request.getFullName())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.CUSTOMER)
                .kycStatus(KycStatus.VERIFIED)
                .emailVerified(false)
                .emailVerifyToken(verifyToken)
                .isActive(true)
                .termsAccepted(request.getTermsAccepted())
                .termsAcceptedAt(LocalDateTime.now())
                .dataProcessingConsent(request.getDataProcessingConsent())
                .consentGiven(true)
                .consentGivenAt(LocalDateTime.now())
                .marketingConsent(request.getMarketingConsent() != null ? request.getMarketingConsent() : false)
                .privacyPolicyVersion("1.0")
                .createdAt(LocalDateTime.now())
                .build();

        user = userRepository.save(user);

        // PERFORMANCE FIX: Use async audit logging for faster response
        auditService.logUserRegisteredAsync(user.getId(), user.getEmail(), user.getRole());
        
        log.info("Customer registered successfully: {} (ID: {})", user.getEmail(), user.getId());
        
        // Send verification email (already async via email service)
        emailService.sendVerificationEmail(user, verifyToken);
        
        return mapToUserResponse(user);
    }

    @Override
    @Transactional
    public UserResponse registerMerchant(MerchantRegisterRequest request) {
        log.info("Processing merchant registration for email: {}", request.getEmail());
        
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email already registered: " + request.getEmail());
        }

        // Generate email verification token
        String verifyToken = generateVerificationToken();

        // Validate consent
        validateConsent(request.getTermsAccepted(), request.getDataProcessingConsent());

        // Create merchant user
        User user = User.builder()
                .email(request.getEmail().toLowerCase().trim())
                .phone(request.getPhone())
                .fullName(request.getFullName())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.MERCHANT)
                .kycStatus(KycStatus.PENDING)
                .emailVerified(false)
                .emailVerifyToken(verifyToken)
                .isActive(true)
                .termsAccepted(request.getTermsAccepted())
                .termsAcceptedAt(LocalDateTime.now())
                .dataProcessingConsent(request.getDataProcessingConsent())
                .consentGiven(true)
                .consentGivenAt(LocalDateTime.now())
                .marketingConsent(request.getMarketingConsent() != null ? request.getMarketingConsent() : false)
                .privacyPolicyVersion("1.0")
                .createdAt(LocalDateTime.now())
                .build();

        user = userRepository.save(user);

        // Create merchant profile
        createMerchantProfile(user, request);
        
        // Create KYC submission record
        createKycSubmission(user.getId(), request);
        
        // PERFORMANCE FIX: Use async audit logging for faster response
        auditService.logKycSubmittedAsync(user.getId(), request.getBusinessName());
        auditService.logUserRegisteredAsync(user.getId(), user.getEmail(), user.getRole());
        
        log.info("Merchant registered successfully: {} (ID: {})", user.getEmail(), user.getId());
        
        // Send verification email
        emailService.sendVerificationEmail(user, verifyToken);
        
        return mapToUserResponse(user);
    }

    /**
     * Legacy registration method (deprecated).
     */
    @Override
    @Deprecated
    @Transactional
    public UserResponse register(RegisterRequest request) {
        throw new UnsupportedOperationException(
            "This method is deprecated. Use registerCustomer() or registerMerchant() instead.");
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = authenticateUser(request);
        return buildAuthResponse(user, "Generic login");
    }

    @Override
    @Transactional
    public AuthResponse loginAdmin(LoginRequest request) {
        User user = authenticateUser(request);
        validateRole(user, UserRole.ADMIN);
        return buildAuthResponse(user, "Admin login");
    }

    @Override
    @Transactional
    public AuthResponse loginCustomer(LoginRequest request) {
        User user = authenticateUser(request);
        validateRole(user, UserRole.CUSTOMER);
        return buildAuthResponse(user, "Customer login");
    }

    @Override
    @Transactional
    public AuthResponse loginMerchant(LoginRequest request) {
        User user = authenticateUser(request);
        validateRole(user, UserRole.MERCHANT);
        return buildAuthResponse(user, "Merchant login");
    }

    /**
     * Core authentication logic shared by all login methods.
     * Validates credentials, checks account status, resets failed attempts,
     * and returns the authenticated User entity.
     * Does NOT generate tokens or log success (callers do that).
     */
    private User authenticateUser(LoginRequest request) {
        long startTime = System.currentTimeMillis();
        long stepStartTime = startTime;
        String email = request.getEmail().toLowerCase().trim();

        // SECURITY: Get request context for audit logging
        RequestContext context = getRequestContext();
        log.info("[LOGIN-PERF] Processing login for email: {} from IP: {}", email, context.ipAddress);

        // Step 1: Find user by email (cache or DB)
        Optional<User> userOpt = userRepository.findByEmail(email);
        long step1Time = System.currentTimeMillis() - stepStartTime;
        stepStartTime = System.currentTimeMillis();
        log.info("[LOGIN-PERF] Step 1 - User lookup by email: {}ms", step1Time);

        // SECURITY: Handle user not found - log failed attempt but don't reveal user existence
        if (userOpt.isEmpty()) {
            auditService.logLoginFailedAsync(email, context.ipAddress, context.userAgent, "USER_NOT_FOUND");
            throw new UnauthorizedException("Invalid email or password");
        }

        User user = userOpt.get();

        // SECURITY: Check if account is locked due to failed attempts
        if (user.isLocked()) {
            long remainingMinutes = java.time.Duration.between(LocalDateTime.now(), user.getLockedUntil()).toMinutes();
            auditService.logLoginFailedAsync(email, context.ipAddress, context.userAgent, "ACCOUNT_LOCKED");
            throw new UnauthorizedException(String.format(
                "Account is temporarily locked due to multiple failed login attempts. Please try again in %d minutes or contact support.",
                remainingMinutes));
        }

        // Step 2: Verify password (BCrypt)
        boolean passwordMatches = passwordEncoder.matches(request.getPassword(), user.getPasswordHash());
        long step2Time = System.currentTimeMillis() - stepStartTime;
        stepStartTime = System.currentTimeMillis();
        log.info("[LOGIN-PERF] Step 2 - Password verification (role={}): {}ms", user.getRole(), step2Time);

        // SECURITY: Handle wrong password - record failed attempt
        if (!passwordMatches) {
            user.recordFailedLogin(securityProperties.getMaxFailedAttempts(), securityProperties.getLockoutMinutes());
            userRepository.save(user);

            auditService.logLoginFailedAsync(email, context.ipAddress, context.userAgent, "INVALID_PASSWORD");

            // Check if this attempt caused a lockout
            if (user.isLocked()) {
                throw new UnauthorizedException(String.format(
                    "Too many failed attempts. Account locked for %d minutes.",
                    securityProperties.getLockoutMinutes()));
            }

            // Calculate remaining attempts
            int remainingAttempts = securityProperties.getMaxFailedAttempts() - user.getFailedLoginAttempts();
            throw new UnauthorizedException(String.format(
                "Invalid email or password. %d attempt(s) remaining before account lockout.",
                remainingAttempts));
        }

        // Step 3: Check account status
        if (!user.getIsActive()) {
            auditService.logLoginFailedAsync(email, context.ipAddress, context.userAgent, "ACCOUNT_DEACTIVATED");
            throw new UnauthorizedException("Account is deactivated. Please contact support.");
        }
        long step3Time = System.currentTimeMillis() - stepStartTime;
        stepStartTime = System.currentTimeMillis();
        log.info("[LOGIN-PERF] Step 3 - Account status check: {}ms", step3Time);

        // SECURITY: Reset failed attempts on successful login
        user.resetFailedLogins();

        // Step 4: Update last login
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
        long step4Time = System.currentTimeMillis() - stepStartTime;
        stepStartTime = System.currentTimeMillis();
        log.info("[LOGIN-PERF] Step 4 - Save user (lastLoginAt update): {}ms", step4Time);

        long totalDuration = System.currentTimeMillis() - startTime;
        log.info("[LOGIN-PERF] User authenticated successfully: {} (ID: {}, role: {}) in {}ms | Breakdown: lookup={}ms, password={}ms, status={}ms, save={}ms",
                user.getEmail(), user.getId(), user.getRole(), totalDuration,
                step1Time, step2Time, step3Time, step4Time);

        return user;
    }

    /**
     * Validates that the authenticated user has the expected role.
     * Logs and throws ForbiddenException if role mismatch.
     */
    private void validateRole(User user, UserRole expectedRole) {
        if (user.getRole() != expectedRole) {
            RequestContext context = getRequestContext();
            auditService.logLoginFailedAsync(user.getEmail(), context.ipAddress, context.userAgent,
                    "ROLE_MISMATCH: expected=" + expectedRole + ", actual=" + user.getRole());
            throw new ForbiddenException(expectedRole.name(), user.getRole().name());
        }
    }

    /**
     * Generates tokens and builds the AuthResponse for a successfully authenticated user.
     */
    private AuthResponse buildAuthResponse(User user, String loginType) {
        long startTime = System.currentTimeMillis();

        // Step 5: Generate JWT access token
        String accessToken = jwtUtil.generateToken(user);
        log.info("[LOGIN-PERF] Step 5 - Generate access token: {}ms", System.currentTimeMillis() - startTime);

        // Step 6: Generate secure refresh token (hashed in DB, rotation-enabled)
        RequestContext context = getRequestContext();
        String refreshToken = refreshTokenService.createRefreshToken(
                user.getId(), context.ipAddress, context.userAgent);
        log.info("[LOGIN-PERF] Step 6 - Generate refresh token: {}ms", System.currentTimeMillis() - startTime);

        // SECURITY: Log successful login with request context (IP, user-agent)
        auditService.logUserLoggedInAsync(user.getId(), user.getEmail(), user.getRole(),
            context.ipAddress, context.userAgent);

        log.info("[LOGIN-PERF] {} successful: {} (ID: {}, role: {})",
                loginType, user.getEmail(), user.getId(), user.getRole());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtUtil.getExpiration())
                .tokenType("Bearer")
                .user(mapToUserResponse(user))
                .build();
    }

    /**
     * Helper class to hold request context
     */
    private record RequestContext(String ipAddress, String userAgent) {}

    /**
     * Extract request context (IP address and User-Agent) from the current HTTP request.
     * Used for security audit logging.
     */
    private RequestContext getRequestContext() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String ipAddress = extractClientIp(request);
                String userAgent = request.getHeader("User-Agent");
                return new RequestContext(ipAddress, userAgent != null ? userAgent : UNKNOWN_USER_AGENT);
            }
        } catch (Exception e) {
            log.debug("Could not extract request context: {}", e.getMessage());
        }
        return new RequestContext(UNKNOWN_IP, UNKNOWN_USER_AGENT);
    }

    /**
     * Extract client IP address from request, handling proxies.
     */
    private String extractClientIp(HttpServletRequest request) {
        String[] headers = {"X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP", 
                           "WL-Proxy-Client-IP", "HTTP_X_FORWARDED_FOR", "HTTP_X_FORWARDED"};
        
        for (String header : headers) {
            String value = request.getHeader(header);
            if (value != null && !value.isEmpty() && !"unknown".equalsIgnoreCase(value)) {
                // X-Forwarded-For can contain multiple IPs - take the first one
                return value.split(",")[0].trim();
            }
        }
        
        return request.getRemoteAddr();
    }

    @Override
    public AuthResponse refreshToken(String refreshToken) {
        log.debug("Processing token refresh");

        RequestContext context = getRequestContext();

        // Rotate refresh token (detects reuse, revokes on compromise)
        RefreshTokenService.RotationResult result;
        try {
            result = refreshTokenService.rotateRefreshToken(
                    refreshToken, context.ipAddress, context.userAgent);
        } catch (RuntimeException e) {
            throw new UnauthorizedException(e.getMessage());
        }

        User user = userRepository.findById(result.userId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!user.getIsActive()) {
            throw new UnauthorizedException("Account is deactivated");
        }

        String newAccessToken = jwtUtil.generateToken(user);

        log.info("Token refreshed for user: {} (ID: {})", user.getEmail(), user.getId());

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(result.newRawToken())
                .expiresIn(jwtUtil.getExpiration())
                .tokenType("Bearer")
                .user(mapToUserResponse(user))
                .build();
    }

    @Override
    @Transactional
    public void verifyEmail(String token) {
        log.info("Processing email verification with token");
        
        if (token == null || token.isBlank()) {
            throw new ValidationException("Verification token is required");
        }

        User user = userRepository.findByEmailVerifyToken(token)
                .orElseThrow(() -> new ValidationException("Invalid or expired verification token"));

        if (user.getEmailVerified()) {
            log.warn("Email already verified for user: {}", user.getEmail());
            return;
        }

        user.setEmailVerified(true);
        user.setEmailVerifyToken(null); // Clear token after use
        userRepository.save(user);

        // PERFORMANCE FIX: Use async audit logging
        auditService.logEmailVerifiedAsync(user.getId(), user.getEmail(), user.getRole());

        log.info("Email verified successfully for user: {} (ID: {})", user.getEmail(), user.getId());
    }

    @Override
    public UserResponse getUserById(String userId) {
        UUID id;
        try {
            id = UUID.fromString(userId);
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Invalid user ID format");
        }
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found with ID: " + userId));
        
        return mapToUserResponse(user);
    }

    @Override
    public UserResponse getUserByApiKey(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new ValidationException("API key is required");
        }

        String identifier = deriveApiKeyIdentifier(apiKey);

        MerchantProfile profile = merchantProfileRepository.findByApiKeyIdentifier(identifier)
                .orElseThrow(() -> new UnauthorizedException("Invalid API key"));
        
        User user = profile.getUser();
        if (!user.getIsActive()) {
            throw new UnauthorizedException("Account is deactivated");
        }
        
        return mapToUserResponse(user, profile);
    }

    @Transactional
    public ApiKeyResponse regenerateApiKey(UUID merchantId) {
        log.info("Regenerating API key for merchant: {}", merchantId);
        
        MerchantProfile profile = merchantProfileRepository.findByUserId(merchantId)
                .orElseThrow(() -> new NotFoundException("Merchant profile not found"));

        User merchant = profile.getUser();

        // Generate new API key
        String newApiKey = generateApiKey();
        String apiKeyHash = passwordEncoder.encode(newApiKey);
        String prefix = newApiKey.substring(0, this.apiKeyPrefix.length() + 8) + "****";
        String identifier = deriveApiKeyIdentifier(newApiKey);

        // Invalidate old key and save new one
        profile.setApiKey(apiKeyHash);
        profile.setApiKeyPrefix(prefix);
        profile.setApiKeyIdentifier(identifier);
        profile.setWebhookSecret(generateWebhookSecret());
        merchantProfileRepository.save(profile);

        // PERFORMANCE FIX: Use async audit logging
        auditService.logApiKeyGeneratedAsync(merchantId, prefix, UserRole.MERCHANT);

        log.info("API key regenerated for merchant: {}", merchantId);

        // Send email notification
        emailService.sendApiKeyRegeneratedEmail(merchant, prefix);

        // Return the full key (only shown once)
        return ApiKeyResponse.builder()
                .apiKey(newApiKey)
                .prefix(prefix)
                .createdAt(LocalDateTime.now())
                .warning("Store this key securely. It will not be shown again.")
                .build();
    }

    @Transactional
    public MerchantResponse approveKyc(UUID merchantId, UUID approvedBy) {
        log.info("Approving KYC for merchant: {} by admin: {}", merchantId, approvedBy);
        
        MerchantProfile profile = merchantProfileRepository.findByUserId(merchantId)
                .orElseThrow(() -> new NotFoundException("Merchant profile not found"));

        User merchant = profile.getUser();
        
        if (merchant.getKycStatus() == KycStatus.VERIFIED) {
            throw new ConflictException("Merchant KYC is already approved");
        }

        // Update KYC status
        merchant.setKycStatus(KycStatus.VERIFIED);
        userRepository.save(merchant);

        // Generate API key for the merchant
        String apiKey = generateApiKey();
        String apiKeyHash = passwordEncoder.encode(apiKey);
        String prefix = apiKey.substring(0, apiKeyPrefix.length() + 8) + "****";
        String identifier = deriveApiKeyIdentifier(apiKey);

        profile.setApiKey(apiKeyHash);
        profile.setApiKeyPrefix(prefix);
        profile.setApiKeyIdentifier(identifier);
        profile.setIsVerified(true);
        profile.setKycReviewedAt(LocalDateTime.now());
        profile.setKycReviewedBy(approvedBy);
        merchantProfileRepository.save(profile);

        // Update KYC submission record
        kycSubmissionRepository.findByMerchantId(merchantId).ifPresent(submission -> {
            submission.setStatus(KycStatus.VERIFIED);
            submission.setReviewedAt(LocalDateTime.now());
            submission.setReviewedBy(approvedBy);
            kycSubmissionRepository.save(submission);
        });

        // PERFORMANCE FIX: Use async audit logging
        auditService.logKycApprovedAsync(merchantId, profile.getBusinessName(), approvedBy);
        auditService.logApiKeyGeneratedAsync(merchantId, prefix, UserRole.ADMIN);

        log.info("KYC approved for merchant: {} ({})", merchantId, profile.getBusinessName());

        // Send approval email
        emailService.sendKycApprovedEmail(merchant, profile.getBusinessName());

        return MerchantResponse.builder()
                .userId(merchantId)
                .businessName(profile.getBusinessName())
                .kycStatus(KycStatus.VERIFIED)
                .apiKey(apiKey) // Full key shown only once
                .apiKeyPrefix(prefix)
                .verifiedAt(profile.getKycReviewedAt())
                .build();
    }

    @Transactional
    public void rejectKyc(UUID merchantId, String reason, UUID rejectedBy) {
        log.info("Rejecting KYC for merchant: {} by admin: {}", merchantId, rejectedBy);
        
        if (reason == null || reason.isBlank()) {
            throw new ValidationException("Rejection reason is required");
        }

        MerchantProfile profile = merchantProfileRepository.findByUserId(merchantId)
                .orElseThrow(() -> new NotFoundException("Merchant profile not found"));

        User merchant = profile.getUser();
        
        if (merchant.getKycStatus() == KycStatus.VERIFIED) {
            throw new ConflictException("Cannot reject already approved KYC");
        }

        // Update KYC status
        merchant.setKycStatus(KycStatus.REJECTED);
        userRepository.save(merchant);

        profile.setKycRejectionReason(reason);
        profile.setKycReviewedAt(LocalDateTime.now());
        profile.setKycReviewedBy(rejectedBy);
        merchantProfileRepository.save(profile);

        // Update KYC submission record
        kycSubmissionRepository.findByMerchantId(merchantId).ifPresent(submission -> {
            submission.setStatus(KycStatus.REJECTED);
            submission.setReviewedAt(LocalDateTime.now());
            submission.setReviewedBy(rejectedBy);
            submission.setRejectionReason(reason);
            kycSubmissionRepository.save(submission);
        });
        
        log.info("KYC rejected for merchant: {}. Reason: {}", merchantId, reason);

        // Send rejection email
        emailService.sendKycRejectedEmail(merchant, profile.getBusinessName(), reason);
    }

    private void createMerchantProfile(User user, MerchantRegisterRequest request) {
        MerchantProfile profile = MerchantProfile.builder()
                .user(user)
                .businessName(request.getBusinessName())
                .businessType(request.getBusinessType())
                .businessRegNumber(request.getBusinessRegNumber())
                .bankAccountNumber(request.getBankAccountNumber())
                .bankCode(request.getBankCode())
                .bankName(request.getBankName())
                .bvn(request.getBvn())
                .settlementEmail(request.getEmail())
                .isVerified(false)
                .kycSubmittedAt(LocalDateTime.now())
                .build();

        merchantProfileRepository.save(profile);
        log.debug("Merchant profile created for user: {}", user.getId());
    }

    private void createKycSubmission(UUID merchantId, MerchantRegisterRequest request) {
        KycSubmission submission = KycSubmission.builder()
                .merchantId(merchantId)
                .status(KycStatus.PENDING)
                .submittedAt(LocalDateTime.now())
                .verificationMethod("MANUAL") // MVP: Manual review of business/bank info
                .businessName(request.getBusinessName())
                .bankAccountNumber(request.getBankAccountNumber())
                .bankName(request.getBankName())
                .bvn(request.getBvn())
                .build();

        kycSubmissionRepository.save(submission);
        log.debug("KYC submission record created for merchant: {}", merchantId);
    }

    private UserResponse mapToUserResponse(User user) {
        UserResponse.UserResponseBuilder builder = UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .phone(user.getPhone())
                .fullName(user.getFullName())
                .role(user.getRole())
                .kycStatus(user.getKycStatus())
                .emailVerified(user.getEmailVerified())
                .createdAt(user.getCreatedAt())
                .lastLoginAt(user.getLastLoginAt());

        // Add merchant-specific fields if applicable
        if (user.getRole() == UserRole.MERCHANT) {
            merchantProfileRepository.findByUserId(user.getId()).ifPresent(profile -> {
                builder.businessName(profile.getBusinessName())
                        .businessType(profile.getBusinessType())
                        .isVerified(profile.getIsVerified())
                        .apiKeyPrefix(profile.getApiKeyPrefix());
            });
        }

        return builder.build();
    }

    private UserResponse mapToUserResponse(User user, MerchantProfile profile) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .phone(user.getPhone())
                .fullName(user.getFullName())
                .role(user.getRole())
                .kycStatus(user.getKycStatus())
                .emailVerified(user.getEmailVerified())
                .createdAt(user.getCreatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .businessName(profile.getBusinessName())
                .businessType(profile.getBusinessType())
                .isVerified(profile.getIsVerified())
                .apiKeyPrefix(profile.getApiKeyPrefix())
                .build();
    }

    private String generateApiKey() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return apiKeyPrefix + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String generateVerificationToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String generateWebhookSecret() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }

    private String deriveApiKeyIdentifier(String plainApiKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(plainApiKey.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    private void validateConsent(Boolean termsAccepted, Boolean dataProcessingConsent) {
        if (termsAccepted == null || !termsAccepted) {
            throw new ValidationException("You must accept the terms of service to register.");
        }
        if (dataProcessingConsent == null || !dataProcessingConsent) {
            throw new ValidationException("You must consent to data processing to register.");
        }
    }
}
