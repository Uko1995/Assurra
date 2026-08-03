package com.uko.eaas.communication.dto;

import com.uko.eaas.communication.model.enums.NotificationPriority;
import com.uko.eaas.communication.model.enums.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateNotificationRequest {

    @NotNull(message = "User ID is required")
    private String userId;

    @NotNull(message = "Notification type is required")
    private NotificationType type;

    private NotificationPriority priority = NotificationPriority.NORMAL;

    private String subject;

    @NotBlank(message = "Body is required")
    private String body;

    private String bodyHtml;

    private String emailTo;

    private String phoneNumber;

    private String templateName;

    private String referenceId;

    private String referenceType;

    // Idempotency key for event-driven notifications
    private String sourceEventId;
}
