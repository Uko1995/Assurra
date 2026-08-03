package com.uko.eaas.communication.dto;

import com.uko.eaas.communication.model.enums.NotificationPriority;
import com.uko.eaas.communication.model.enums.NotificationStatus;
import com.uko.eaas.communication.model.enums.NotificationType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class NotificationResponse {

    private UUID id;
    private UUID userId;
    private NotificationType type;
    private NotificationStatus status;
    private NotificationPriority priority;

    private String subject;
    private String body;

    private String referenceId;
    private String referenceType;

    private LocalDateTime sentAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime readAt;

    private LocalDateTime createdAt;
}
