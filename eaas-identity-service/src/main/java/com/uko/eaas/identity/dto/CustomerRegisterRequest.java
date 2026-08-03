package com.uko.eaas.identity.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Request DTO for customer registration.
 * Customers can immediately use the platform after email verification.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CustomerRegisterRequest extends RegisterRequest {

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 255, message = "Full name must be between 2 and 255 characters")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Phone is required")
    @Size(min = 10, max = 20, message = "Phone must be between 10 and 20 characters")
    private String phone;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @jakarta.validation.constraints.NotNull(message = "You must accept the terms of service")
    private Boolean termsAccepted;

    @jakarta.validation.constraints.NotNull(message = "You must consent to data processing")
    private Boolean dataProcessingConsent;

    private Boolean marketingConsent = false;

    /**
     * Sample request body:
     * {
     *   "fullName": "John Doe",
     *   "email": "[EMAIL_REDACTED]",
     *   "phone": "[PHONE NUMBER_REDACTED]",
     *   "password": "SecurePass123!",
     *   "termsAccepted": true,
     *   "dataProcessingConsent": true,
     *   "marketingConsent": false
     * }
     */
}
