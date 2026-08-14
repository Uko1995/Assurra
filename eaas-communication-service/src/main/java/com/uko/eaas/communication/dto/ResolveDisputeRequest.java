package com.uko.eaas.communication.dto;

import com.uko.eaas.communication.model.enums.DisputeStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResolveDisputeRequest {

    @NotNull(message = "Resolution status is required")
    private DisputeStatus resolution;

    private BigDecimal resolutionAmount;

    private String resolutionNotes;
}
