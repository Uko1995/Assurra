package com.uko.eaas.payment.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class InitializePaymentRequest {

    @NotBlank(message = "Escrow reference is required")
    private String escrowReference;

    @NotBlank(message = "Customer email is required")
    @Email(message = "Invalid email format")
    private String customerEmail;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "100.00", message = "Minimum amount is ₦100")
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    private String currency = "NGN";

    private String callbackUrl;

    private String idempotencyKey;
}
