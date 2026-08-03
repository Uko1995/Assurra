package com.uko.eaas.communication.dto;

import com.uko.eaas.communication.model.enums.DisputeReason;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateDisputeRequest {

    @NotBlank(message = "Escrow reference is required")
    private String escrowReference;

    @NotBlank(message = "Customer ID is required")
    private String customerId;

    @NotBlank(message = "Merchant ID is required")
    private String merchantId;

    @NotBlank(message = "Raised by is required")
    private String raisedBy;

    @NotNull(message = "Reason is required")
    private DisputeReason reason;

    @NotBlank(message = "Description is required")
    @Size(max = 5000, message = "Description cannot exceed 5000 characters")
    private String description;

    @Size(max = 2000, message = "Desired outcome cannot exceed 2000 characters")
    private String desiredOutcome;

    @NotNull(message = "Amount disputed is required")
    @DecimalMin(value = "1.00", message = "Amount must be at least ₦1")
    private BigDecimal amountDisputed;
}
