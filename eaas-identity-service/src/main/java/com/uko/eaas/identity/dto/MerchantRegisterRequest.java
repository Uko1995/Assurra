package com.uko.eaas.identity.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Request DTO for merchant registration.
 * Merchants require KYC approval before they can receive payments.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MerchantRegisterRequest extends RegisterRequest {

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

    // Merchant Business Details
    @NotBlank(message = "Business name is required")
    @Size(min = 3, max = 100, message = "Business name must be between 3 and 100 characters")
    private String businessName;

    private String businessType;

    private String businessRegNumber;

    // Banking Details
    @NotBlank(message = "Bank account number is required")
    @Pattern(regexp = "\\d{10}", message = "Bank account number must be 10 digits")
    private String bankAccountNumber;

    @NotBlank(message = "Bank code is required")
    private String bankCode;

    @NotBlank(message = "Bank name is required")
    private String bankName;

    @NotBlank(message = "BVN is required")
    @Pattern(regexp = "\\d{11}", message = "BVN must be 11 digits")
    private String bvn;

    /**
     * Sample request body:
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
     */
}
