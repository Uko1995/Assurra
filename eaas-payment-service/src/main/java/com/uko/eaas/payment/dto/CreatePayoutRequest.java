package com.uko.eaas.payment.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CreatePayoutRequest {

    @NotBlank(message = "Escrow reference is required")
    private String escrowReference;

    @NotNull(message = "Merchant ID is required")
    private String merchantId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "100.00", message = "Minimum payout amount is ₦100")
    private BigDecimal amount;

    @NotBlank(message = "Bank code is required")
    @Size(min = 3, max = 10, message = "Bank code must be 3-10 characters")
    private String bankCode;

    @NotBlank(message = "Bank name is required")
    private String bankName;

    @NotBlank(message = "Account number is required")
    @Size(min = 10, max = 10, message = "Account number must be 10 digits")
    @Pattern(regexp = "\\d{10}", message = "Account number must contain only digits")
    private String accountNumber;

    @NotBlank(message = "Account name is required")
    private String accountName;

    private LocalDateTime scheduledAt;
}
