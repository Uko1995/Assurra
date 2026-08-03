package com.uko.eaas.escrow.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateEscrowRequest {

    @NotBlank(message = "Merchant ID is required")
    private String merchantId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "100.00", message = "Minimum amount is ₦100")
    @DecimalMax(value = "10000000.00", message = "Maximum amount is ₦10,000,000")
    private BigDecimal amount;

    @NotBlank(message = "Product description is required")
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String productDescription;

    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer productQuantity = 1;

    @Min(value = 1, message = "Delivery days must be at least 1")
    @Max(value = 30, message = "Delivery days cannot exceed 30")
    private Integer agreedDeliveryDays = 7;
}
