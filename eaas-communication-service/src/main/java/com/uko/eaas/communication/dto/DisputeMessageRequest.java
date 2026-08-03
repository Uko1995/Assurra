package com.uko.eaas.communication.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class DisputeMessageRequest {

    @NotNull(message = "Dispute ID is required")
    private UUID disputeId;

    @NotNull(message = "Sender ID is required")
    private UUID senderId;

    @NotBlank(message = "Sender type is required")
    private String senderType;

    @NotBlank(message = "Message is required")
    private String message;

    private Boolean isInternal = false;

    private List<UUID> attachmentIds;
}
