package com.uko.eaas.communication.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class DisputeMessageResponse {

    private UUID id;
    private UUID disputeId;
    private UUID senderId;
    private String senderType;

    private String message;
    private Boolean isInternal;

    private Boolean hasAttachments;
    private List<UUID> attachmentIds;

    private Boolean readByCustomer;
    private Boolean readByMerchant;
    private LocalDateTime readAt;

    private LocalDateTime createdAt;
}
