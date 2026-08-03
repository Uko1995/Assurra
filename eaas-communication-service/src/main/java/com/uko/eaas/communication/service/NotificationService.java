package com.uko.eaas.communication.service;

import com.uko.eaas.communication.dto.CreateNotificationRequest;
import com.uko.eaas.communication.dto.NotificationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface NotificationService {

    NotificationResponse createNotification(CreateNotificationRequest request);

    NotificationResponse getNotification(UUID id);

    Page<NotificationResponse> listNotifications(UUID userId, Pageable pageable);

    long countUnreadNotifications(UUID userId);

    void markAsRead(UUID notificationId);

    void markAllAsRead(UUID userId);

    void sendPendingNotifications();

    void sendEmailNotification(UUID notificationId);

    void sendSmsNotification(UUID notificationId);

    void sendNotificationForEvent(String eventType, String userId, String referenceId, Object data);

    /**
     * Send notification for an event with idempotency key.
     *
     * @param eventType     the event type
     * @param userId        the target user id
     * @param referenceId   the reference id (escrow/payment reference)
     * @param data          additional event data
     * @param sourceEventId idempotency key (eventType + reference + suffix)
     */
    void sendNotificationForEvent(String eventType, String userId, String referenceId, Object data, String sourceEventId);
}
