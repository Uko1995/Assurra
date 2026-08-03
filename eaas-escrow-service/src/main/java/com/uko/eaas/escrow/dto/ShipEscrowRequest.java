package com.uko.eaas.escrow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ShipEscrowRequest {

    @NotBlank(message = "Tracking number is required")
    @Size(max = 255, message = "Tracking number cannot exceed 255 characters")
    private String trackingNumber;

    @NotBlank(message = "Logistics provider is required")
    @Size(max = 100, message = "Logistics provider cannot exceed 100 characters")
    private String logisticsProvider;

    private LocalDateTime estimatedDeliveryDate;
}
