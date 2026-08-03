package com.uko.eaas.communication.dto;

import com.uko.eaas.communication.model.enums.DisputeStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ResolveDisputeRequest {

    @NotNull(message = "Resolution status is required")
    private DisputeStatus resolution;

    private BigDecimal resolutionAmount;

    private String resolutionNotes;
}
