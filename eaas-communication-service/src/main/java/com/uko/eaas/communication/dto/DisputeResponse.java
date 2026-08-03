package com.uko.eaas.communication.dto;

import com.uko.eaas.communication.model.enums.DisputeReason;
import com.uko.eaas.communication.model.enums.DisputeStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class DisputeResponse {

    private UUID id;
    private String reference;
    private String escrowReference;

    private UUID customerId;
    private UUID merchantId;
    private UUID raisedBy;

    private DisputeReason reason;
    private String description;
    private String desiredOutcome;

    private DisputeStatus status;
    private BigDecimal amountDisputed;
    private BigDecimal resolutionAmount;

    private String resolutionNotes;
    private UUID resolvedBy;
    private LocalDateTime resolvedAt;

    private LocalDateTime openedAt;
    private LocalDateTime closedAt;
    private LocalDateTime lastActivityAt;

    private Boolean customerNotified;
    private Boolean merchantNotified;

    private LocalDateTime createdAt;
}
