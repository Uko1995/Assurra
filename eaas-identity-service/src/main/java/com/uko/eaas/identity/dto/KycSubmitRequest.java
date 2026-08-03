package com.uko.eaas.identity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request DTO for merchant KYC submission/resubmission.
 * Used by merchants to submit or update their KYC details after registration.
 */
@Data
public class KycSubmitRequest {

    @NotBlank(message = "Business name is required")
    @Size(min = 3, max = 100, message = "Business name must be between 3 and 100 characters")
    private String businessName;

    private String businessType;

    private String businessRegNumber;

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
}
