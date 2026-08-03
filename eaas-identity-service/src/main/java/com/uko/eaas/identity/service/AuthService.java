package com.uko.eaas.identity.service;

import com.uko.eaas.identity.dto.*;

/**
 * Authentication Service Interface
 * Provides authentication and user management operations.
 */
public interface AuthService {

    /**
     * Register a new customer.
     *
     * @param request Customer registration details
     * @return Registered user
     */
    UserResponse registerCustomer(CustomerRegisterRequest request);

    /**
     * Register a new merchant.
     *
     * @param request Merchant registration details
     * @return Registered merchant with pending KYC status
     */
    UserResponse registerMerchant(MerchantRegisterRequest request);

    /**
     * Legacy registration method (deprecated).
     * Use registerCustomer() or registerMerchant() instead.
     *
     * @deprecated Use specific registration methods
     */
    @Deprecated
    UserResponse register(RegisterRequest request);

    /**
     * Authenticate user and generate tokens (generic - any role).
     *
     * @param request Login credentials
     * @return Authentication response with tokens
     */
    AuthResponse login(LoginRequest request);

    /**
     * Authenticate admin and generate tokens.
     * Returns 403 if the user is not an ADMIN.
     *
     * @param request Login credentials
     * @return Authentication response with tokens
     */
    AuthResponse loginAdmin(LoginRequest request);

    /**
     * Authenticate customer and generate tokens.
     * Returns 403 if the user is not a CUSTOMER.
     *
     * @param request Login credentials
     * @return Authentication response with tokens
     */
    AuthResponse loginCustomer(LoginRequest request);

    /**
     * Authenticate merchant and generate tokens.
     * Returns 403 if the user is not a MERCHANT.
     *
     * @param request Login credentials
     * @return Authentication response with tokens
     */
    AuthResponse loginMerchant(LoginRequest request);

    /**
     * Refresh access token.
     *
     * @param refreshToken Valid refresh token
     * @return New authentication response
     */
    AuthResponse refreshToken(String refreshToken);

    /**
     * Verify email address.
     *
     * @param token Email verification token
     */
    void verifyEmail(String token);

    /**
     * Get user by ID.
     *
     * @param userId User UUID
     * @return User details
     */
    UserResponse getUserById(String userId);

    /**
     * Get user by API key.
     *
     * @param apiKey Merchant API key
     * @return User details
     */
    UserResponse getUserByApiKey(String apiKey);
}
