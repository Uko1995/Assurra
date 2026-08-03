package com.uko.eaas.escrow.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EscrowDisputeResolutionRequest {

    @NotBlank(message = "Resolution is required")
    private String resolution;
}