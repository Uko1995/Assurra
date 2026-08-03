package com.uko.eaas.identity.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request DTO for admin user creation.
 * Only existing admins can create new admin accounts.
 */
@Data
public class AdminRegisterRequest {

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

    /**
     * Sample request body:
     * {
     *   "fullName": "Admin User",
     *   "email": "[EMAIL_REDACTED]",
     *   "phone": "[PHONE NUMBER_REDACTED]",
     *   "password": "SecureAdminPass123!"
     * }
     */
}
