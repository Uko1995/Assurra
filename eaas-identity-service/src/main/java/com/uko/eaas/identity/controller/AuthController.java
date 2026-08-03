package com.uko.eaas.identity.controller;

import com.uko.eaas.identity.dto.*;
import com.uko.eaas.identity.model.enums.UserRole;
import com.uko.eaas.identity.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Authentication Controller
 * Provides endpoints for user authentication and registration.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Register a new customer.
     * Customers can immediately use the platform after email verification.
     *
     * @param request Customer registration details
     * @return Created user with success message
     *
     * Sample Request:
     * POST /api/v1/auth/register/customer
     * {
     *   "fullName": "John Doe",
     *   "email": "[EMAIL_REDACTED]",
     *   "phone": "[PHONE NUMBER_REDACTED]",
     *   "password": "SecurePass123!"
     * }
     *
     * Sample Response (201 Created):
     * {
     *   "success": true,
     *   "message": "Registration successful. Please verify your email.",
     *   "data": {
     *     "id": "550e8400-e29b-41d4-a716-446655440000",
     *     "email": "[EMAIL_REDACTED]",
     *     "fullName": "John Doe",
     *     "role": "CUSTOMER",
     *     "kycStatus": "VERIFIED",
     *     "emailVerified": false,
     *     "createdAt": "2024-01-15T10:30:00"
     *   }
     * }
     */
    @PostMapping("/register/customer")
    public ResponseEntity<ApiResponse<UserResponse>> registerCustomer(
            @Valid @RequestBody CustomerRegisterRequest request) {
        log.info("Customer registration attempt for email: {}", request.getEmail());
        UserResponse user = authService.registerCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Registration successful. Please verify your email.", user));
    }

    /**
     * Register a new merchant.
     * Merchants require KYC approval before they can receive payments.
     *
     * @param request Merchant registration details
     * @return Created merchant with pending KYC status
     *
     * Sample Request:
     * POST /api/v1/auth/register/merchant
     * {
     *   "fullName": "Jane Smith",
     *   "email": "[EMAIL_REDACTED]",
     *   "phone": "[PHONE NUMBER_REDACTED]",
     *   "password": "SecurePass123!",
     *   "businessName": "Jane's Electronics",
     *   "businessType": "Retail",
     *   "businessRegNumber": "RC123456",
     *   "bankAccountNumber": "[PHONE NUMBER_REDACTED]",
     *   "bankCode": "058",
     *   "bankName": "Guaranty Trust Bank",
     *   "bvn": "12345678901"
     * }
     *
     * Sample Response (202 Accepted):
     * {
     *   "success": true,
     *   "message": "Registration submitted. Your KYC is pending review.",
     *   "data": {
     *     "id": "550e8400-e29b-41d4-a716-446655440001",
     *     "email": "[EMAIL_REDACTED]",
     *     "fullName": "Jane Smith",
     *     "role": "MERCHANT",
     *     "kycStatus": "PENDING",
     *     "businessName": "Jane's Electronics",
     *     "emailVerified": false,
     *     "createdAt": "2024-01-15T10:35:00"
     *   }
     * }
     */
    @PostMapping("/register/merchant")
    public ResponseEntity<ApiResponse<UserResponse>> registerMerchant(
            @Valid @RequestBody MerchantRegisterRequest request) {
        log.info("Merchant registration attempt for email: {}", request.getEmail());
        UserResponse user = authService.registerMerchant(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                    "Registration submitted. Your KYC is pending review. You will receive an API key once approved.",
                    user));
    }

    /**
     * Legacy registration endpoint (deprecated).
     * Use /register/customer or /register/merchant instead.
     *
     * @deprecated Use specific registration endpoints
     */
    @Deprecated
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        log.warn("Deprecated /register endpoint called. Please use /register/customer or /register/merchant");
        
        // Forward to appropriate method based on role
        if (request instanceof CustomerRegisterRequest) {
            return registerCustomer((CustomerRegisterRequest) request);
        } else if (request instanceof MerchantRegisterRequest) {
            return registerMerchant((MerchantRegisterRequest) request);
        } else {
            throw new IllegalArgumentException("Invalid request type. Use CustomerRegisterRequest or MerchantRegisterRequest");
        }
    }

    /**
     * Generic login - authenticates any user role.
     * Use role-specific endpoints below for stricter validation.
     *
     * @param request Login credentials
     * @return Authentication tokens
     *
     * Sample Request:
     * POST /api/v1/auth/login
     * {
     *   "email": "[EMAIL_REDACTED]",
     *   "password": "SecurePass123!"
     * }
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());
        AuthResponse auth = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", auth));
    }

    /**
     * Admin login - only ADMIN users can authenticate.
     * Returns 403 Forbidden if the user exists but is not an ADMIN.
     *
     * @param request Login credentials
     * @return Authentication tokens
     *
     * POST /api/v1/auth/login/admin
     */
    @PostMapping("/login/admin")
    public ResponseEntity<ApiResponse<AuthResponse>> loginAdmin(
            @Valid @RequestBody LoginRequest request) {
        log.info("Admin login attempt for email: {}", request.getEmail());
        AuthResponse auth = authService.loginAdmin(request);
        return ResponseEntity.ok(ApiResponse.success("Admin login successful", auth));
    }

    /**
     * Customer login - only CUSTOMER users can authenticate.
     * Returns 403 Forbidden if the user exists but is not a CUSTOMER.
     *
     * @param request Login credentials
     * @return Authentication tokens
     *
     * POST /api/v1/auth/login/customer
     */
    @PostMapping("/login/customer")
    public ResponseEntity<ApiResponse<AuthResponse>> loginCustomer(
            @Valid @RequestBody LoginRequest request) {
        log.info("Customer login attempt for email: {}", request.getEmail());
        AuthResponse auth = authService.loginCustomer(request);
        return ResponseEntity.ok(ApiResponse.success("Customer login successful", auth));
    }

    /**
     * Merchant login - only MERCHANT users can authenticate.
     * Returns 403 Forbidden if the user exists but is not a MERCHANT.
     *
     * @param request Login credentials
     * @return Authentication tokens
     *
     * POST /api/v1/auth/login/merchant
     */
    @PostMapping("/login/merchant")
    public ResponseEntity<ApiResponse<AuthResponse>> loginMerchant(
            @Valid @RequestBody LoginRequest request) {
        log.info("Merchant login attempt for email: {}", request.getEmail());
        AuthResponse auth = authService.loginMerchant(request);
        return ResponseEntity.ok(ApiResponse.success("Merchant login successful", auth));
    }

    /**
     * Refresh access token using refresh token.
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse auth = authService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success("Token refreshed", auth));
    }

    /**
     * Verify email address using verification token.
     */
    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(
            @RequestParam String token) {
        authService.verifyEmail(token);
        return ResponseEntity.ok(ApiResponse.success("Email verified successfully", null));
    }

    /**
     * Get current authenticated user details.
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(
            @RequestHeader("X-User-Id") String userId) {
        UserResponse user = authService.getUserById(userId);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    // Health check endpoint
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "identity-service");
        return ResponseEntity.ok(response);
    }
}
