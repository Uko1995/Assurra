package com.uko.eaas.communication.service.impl;

import com.uko.eaas.communication.client.IdentityServiceClient;
import com.uko.eaas.communication.dto.CreateNotificationRequest;
import com.uko.eaas.communication.dto.NotificationResponse;
import com.uko.eaas.communication.model.entity.Notification;
import com.uko.eaas.communication.model.enums.NotificationPriority;
import com.uko.eaas.communication.model.enums.NotificationStatus;
import com.uko.eaas.communication.model.enums.NotificationType;
import com.uko.eaas.communication.repository.NotificationRepository;
import com.uko.eaas.communication.service.EmailService;
import com.uko.eaas.communication.service.NotificationService;
import com.uko.eaas.communication.service.SmsService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    private final SmsService smsService;
    private final IdentityServiceClient identityServiceClient;

    @Override
    public NotificationResponse createNotification(CreateNotificationRequest request) {
        log.info("Creating notification for user: {}, type: {}", request.getUserId(), request.getType());

        // Idempotency check for event-driven notifications
        if (request.getSourceEventId() != null && !request.getSourceEventId().isBlank()) {
            UUID userUuid = UUID.fromString(request.getUserId());
            if (notificationRepository.existsByUserIdAndSourceEventId(userUuid, request.getSourceEventId())) {
                log.info("Skipping duplicate notification for user: {}, sourceEventId: {}",
                        request.getUserId(), request.getSourceEventId());
                return null;
            }
        }

        // Resolve missing contact details from the identity service
        if (request.getType() == NotificationType.EMAIL
                && (request.getEmailTo() == null || request.getEmailTo().isBlank())) {
            identityServiceClient.getUserContact(request.getUserId()).ifPresent(contact -> {
                if (contact.getEmail() != null && !contact.getEmail().isBlank()) {
                    request.setEmailTo(contact.getEmail());
                }
            });
        }
        if (request.getType() == NotificationType.SMS
                && (request.getPhoneNumber() == null || request.getPhoneNumber().isBlank())) {
            identityServiceClient.getUserContact(request.getUserId()).ifPresent(contact -> {
                if (contact.getPhoneNumber() != null && !contact.getPhoneNumber().isBlank()) {
                    request.setPhoneNumber(contact.getPhoneNumber());
                }
            });
        }

        Notification notification = Notification.builder()
                .userId(UUID.fromString(request.getUserId()))
                .type(request.getType())
                .priority(request.getPriority())
                .subject(request.getSubject())
                .body(request.getBody())
                .bodyHtml(request.getBodyHtml())
                .emailTo(request.getEmailTo())
                .phoneNumber(request.getPhoneNumber())
                .templateName(request.getTemplateName())
                .referenceId(request.getReferenceId())
                .referenceType(request.getReferenceType())
                .sourceEventId(request.getSourceEventId())
                .status(NotificationStatus.PENDING)
                .build();

        notification = notificationRepository.save(notification);

        // Send immediately for high priority
        if (request.getPriority() != null && request.getPriority().ordinal() >= 2) {
            processNotification(notification);
        }

        return mapToResponse(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationResponse getNotification(UUID id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Notification not found: " + id));
        return mapToResponse(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> listNotifications(UUID userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public long countUnreadNotifications(UUID userId) {
        return notificationRepository.countUnreadByUser(userId);
    }

    @Override
    public void markAsRead(UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new EntityNotFoundException("Notification not found: " + notificationId));

        notification.setReadAt(LocalDateTime.now());
        notification.setStatus(NotificationStatus.READ);
        notificationRepository.save(notification);

        log.info("Marked notification {} as read", notificationId);
    }

    @Override
    public void markAllAsRead(UUID userId) {
        List<Notification> unreadNotifications = notificationRepository
                .findByUserIdAndStatus(userId, NotificationStatus.DELIVERED);

        LocalDateTime now = LocalDateTime.now();
        for (Notification notification : unreadNotifications) {
            notification.setReadAt(now);
            notification.setStatus(NotificationStatus.READ);
        }

        notificationRepository.saveAll(unreadNotifications);
        log.info("Marked all notifications as read for user: {}", userId);
    }

    @Override
    public void sendPendingNotifications() {
        log.debug("Processing pending notifications");

        List<Notification> pendingNotifications = notificationRepository.findByStatus(NotificationStatus.PENDING);

        for (Notification notification : pendingNotifications) {
            try {
                processNotification(notification);
            } catch (Exception e) {
                log.error("Failed to process notification {}: {}", notification.getId(), e.getMessage());

                notification.setStatus(NotificationStatus.FAILED);
                notification.setFailedAt(LocalDateTime.now());
                notification.setFailureReason(e.getMessage());
                notificationRepository.save(notification);
            }
        }
    }

    @Override
    public void sendEmailNotification(UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new EntityNotFoundException("Notification not found: " + notificationId));

        if (notification.getEmailTo() == null || notification.getEmailTo().isBlank()) {
            log.warn("Skipping email notification {}: no email address specified", notificationId);
            notification.setStatus(NotificationStatus.FAILED);
            notification.setFailedAt(LocalDateTime.now());
            notification.setFailureReason("No email address specified");
            notificationRepository.save(notification);
            return;
        }

        try {
            if (notification.getBodyHtml() != null) {
                emailService.sendHtmlEmail(notification.getEmailTo(), notification.getSubject(), notification.getBodyHtml());
            } else {
                emailService.sendEmail(notification.getEmailTo(), notification.getSubject(), notification.getBody());
            }

            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
            notificationRepository.save(notification);

            log.info("Email notification sent: {}", notificationId);

        } catch (Exception e) {
            log.error("Failed to send email notification {}: {}", notificationId, e.getMessage());
            notification.setStatus(NotificationStatus.FAILED);
            notification.setFailedAt(LocalDateTime.now());
            notification.setFailureReason(e.getMessage());
            notificationRepository.save(notification);
        }
    }

    @Override
    public void sendSmsNotification(UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new EntityNotFoundException("Notification not found: " + notificationId));

        if (notification.getPhoneNumber() == null || notification.getPhoneNumber().isBlank()) {
            log.warn("Skipping SMS notification {}: no phone number specified", notificationId);
            notification.setStatus(NotificationStatus.FAILED);
            notification.setFailedAt(LocalDateTime.now());
            notification.setFailureReason("No phone number specified");
            notificationRepository.save(notification);
            return;
        }

        try {
            smsService.sendSms(notification.getPhoneNumber(), notification.getBody());

            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
            notificationRepository.save(notification);

            log.info("SMS notification sent: {}", notificationId);

        } catch (Exception e) {
            log.error("Failed to send SMS notification {}: {}", notificationId, e.getMessage());
            notification.setStatus(NotificationStatus.FAILED);
            notification.setFailedAt(LocalDateTime.now());
            notification.setFailureReason(e.getMessage());
            notificationRepository.save(notification);
        }
    }

    @Override
    public void sendNotificationForEvent(String eventType, String userId, String referenceId, Object data) {
        sendNotificationForEvent(eventType, userId, referenceId, data, null);
    }

    @Override
    public void sendNotificationForEvent(String eventType, String userId, String referenceId, Object data, String sourceEventId) {
        // Map events to notification templates
        switch (eventType) {
            case "escrow.created" ->
                    createEscrowCreatedNotification(userId, referenceId, sourceEventId);
            case "escrow.funded" ->
                    createEscrowFundedNotification(userId, referenceId, sourceEventId);
            case "escrow.shipped" ->
                    createEscrowShippedNotification(userId, referenceId, sourceEventId);
            case "escrow.delivered" ->
                    createEscrowDeliveredNotification(userId, referenceId, sourceEventId);
            case "escrow.confirmed" ->
                    createEscrowConfirmedNotification(userId, referenceId, sourceEventId);
            case "escrow.auto-released" ->
                    createEscrowAutoReleasedNotification(userId, referenceId, sourceEventId);
            case "escrow.cancelled.refund" ->
                    createEscrowCancelledRefundNotification(userId, referenceId, sourceEventId);
            case "escrow.expired" ->
                    createEscrowExpiredNotification(userId, referenceId, sourceEventId);
            case "payment.refunded" ->
                    createPaymentRefundedNotification(userId, referenceId, sourceEventId);
            case "payout.completed" ->
                    createPayoutCompletedNotification(userId, referenceId, sourceEventId);
            case "payout.failed" ->
                    createPayoutFailedNotification(userId, referenceId, sourceEventId);
            case "dispute.opened" ->
                    createDisputeOpenedNotification(userId, referenceId, sourceEventId);
            case "dispute.resolved" ->
                    createDisputeResolvedNotification(userId, referenceId, sourceEventId);
            default ->
                    log.warn("No notification handler for event: {}", eventType);
        }
    }

    private void processNotification(Notification notification) {
        switch (notification.getType()) {
            case EMAIL ->
                    sendEmailNotification(notification.getId());
            case SMS ->
                    sendSmsNotification(notification.getId());
            case PUSH -> {
                // Push notification implementation would go here
                log.info("Push notification would be sent: {}", notification.getId());
            }
            case IN_APP -> {
                notification.setStatus(NotificationStatus.DELIVERED);
                notification.setDeliveredAt(LocalDateTime.now());
                notificationRepository.save(notification);
            }
        }
    }

    private void createEscrowCreatedNotification(String userId, String referenceId, String sourceEventId) {
        CreateNotificationRequest request = new CreateNotificationRequest();
        request.setUserId(userId);
        request.setType(NotificationType.EMAIL);
        request.setSubject("Escrow Transaction Created");
        request.setBody("Your escrow transaction " + referenceId + " has been created. Please complete payment within 24 hours.");
        request.setReferenceId(referenceId);
        request.setReferenceType("ESCROW");
        request.setSourceEventId(sourceEventId);
        createNotification(request);
    }

    private void createEscrowFundedNotification(String userId, String referenceId, String sourceEventId) {
        CreateNotificationRequest request = new CreateNotificationRequest();
        request.setUserId(userId);
        request.setType(NotificationType.EMAIL);
        request.setSubject("Payment Received - Escrow Funded");
        request.setBody("Payment received for escrow " + referenceId + ". The merchant has been notified to ship your order.");
        request.setReferenceId(referenceId);
        request.setReferenceType("ESCROW");
        request.setSourceEventId(sourceEventId);
        createNotification(request);
    }

    private void createEscrowShippedNotification(String userId, String referenceId, String sourceEventId) {
        CreateNotificationRequest request = new CreateNotificationRequest();
        request.setUserId(userId);
        request.setType(NotificationType.EMAIL);
        request.setSubject("Order Shipped");
        request.setBody("Your order for escrow " + referenceId + " has been shipped. You can track it using the tracking number provided.");
        request.setReferenceId(referenceId);
        request.setReferenceType("ESCROW");
        request.setSourceEventId(sourceEventId);
        createNotification(request);
    }

    private void createEscrowDeliveredNotification(String userId, String referenceId, String sourceEventId) {
        CreateNotificationRequest request = new CreateNotificationRequest();
        request.setUserId(userId);
        request.setType(NotificationType.EMAIL);
        request.setPriority(NotificationPriority.HIGH);
        request.setSubject("Order Delivered - Action Required");
        request.setBody("Your order for escrow " + referenceId + " has been marked as delivered. Please confirm receipt or open a dispute within 72 hours.");
        request.setReferenceId(referenceId);
        request.setReferenceType("ESCROW");
        request.setSourceEventId(sourceEventId);
        createNotification(request);
    }

    private void createEscrowConfirmedNotification(String userId, String referenceId, String sourceEventId) {
        CreateNotificationRequest request = new CreateNotificationRequest();
        request.setUserId(userId);
        request.setType(NotificationType.EMAIL);
        request.setSubject("Escrow Confirmed - Payout Processing");
        request.setBody("Escrow " + referenceId + " has been confirmed. Payout will be processed within 24 hours.");
        request.setReferenceId(referenceId);
        request.setReferenceType("ESCROW");
        request.setSourceEventId(sourceEventId);
        createNotification(request);
    }

    private void createEscrowAutoReleasedNotification(String userId, String referenceId, String sourceEventId) {
        CreateNotificationRequest request = new CreateNotificationRequest();
        request.setUserId(userId);
        request.setType(NotificationType.EMAIL);
        request.setSubject("Escrow Auto-Released");
        request.setBody("Escrow " + referenceId + " has been auto-released after the confirmation window expired. Payout is being processed.");
        request.setReferenceId(referenceId);
        request.setReferenceType("ESCROW");
        request.setSourceEventId(sourceEventId);
        createNotification(request);
    }

    private void createEscrowCancelledRefundNotification(String userId, String referenceId, String sourceEventId) {
        CreateNotificationRequest request = new CreateNotificationRequest();
        request.setUserId(userId);
        request.setType(NotificationType.EMAIL);
        request.setSubject("Escrow Cancelled - Refund Processing");
        request.setBody("Escrow " + referenceId + " has been cancelled. Your refund is being processed and will be credited within 5-7 business days.");
        request.setReferenceId(referenceId);
        request.setReferenceType("ESCROW");
        request.setSourceEventId(sourceEventId);
        createNotification(request);
    }

    private void createEscrowExpiredNotification(String userId, String referenceId, String sourceEventId) {
        CreateNotificationRequest request = new CreateNotificationRequest();
        request.setUserId(userId);
        request.setType(NotificationType.EMAIL);
        request.setSubject("Escrow Expired");
        request.setBody("Escrow " + referenceId + " has expired due to non-payment within 24 hours. No charges were made.");
        request.setReferenceId(referenceId);
        request.setReferenceType("ESCROW");
        request.setSourceEventId(sourceEventId);
        createNotification(request);
    }

    private void createPaymentRefundedNotification(String userId, String referenceId, String sourceEventId) {
        CreateNotificationRequest request = new CreateNotificationRequest();
        request.setUserId(userId);
        request.setType(NotificationType.EMAIL);
        request.setSubject("Refund Processed");
        request.setBody("Your refund for payment " + referenceId + " has been processed and will be credited within 5-7 business days.");
        request.setReferenceId(referenceId);
        request.setReferenceType("PAYMENT");
        request.setSourceEventId(sourceEventId);
        createNotification(request);
    }

    private void createPayoutCompletedNotification(String userId, String referenceId, String sourceEventId) {
        CreateNotificationRequest request = new CreateNotificationRequest();
        request.setUserId(userId);
        request.setType(NotificationType.EMAIL);
        request.setSubject("Payout Completed");
        request.setBody("Your payout " + referenceId + " has been completed. Funds have been transferred to your registered bank account.");
        request.setReferenceId(referenceId);
        request.setReferenceType("PAYOUT");
        request.setSourceEventId(sourceEventId);
        createNotification(request);
    }

    private void createPayoutFailedNotification(String userId, String referenceId, String sourceEventId) {
        CreateNotificationRequest request = new CreateNotificationRequest();
        request.setUserId(userId);
        request.setType(NotificationType.EMAIL);
        request.setPriority(NotificationPriority.HIGH);
        request.setSubject("Payout Failed - Action Required");
        request.setBody("Your payout " + referenceId + " could not be processed. Please verify your bank details and contact support.");
        request.setReferenceId(referenceId);
        request.setReferenceType("PAYOUT");
        request.setSourceEventId(sourceEventId);
        createNotification(request);
    }

    private void createDisputeOpenedNotification(String userId, String referenceId, String sourceEventId) {
        CreateNotificationRequest request = new CreateNotificationRequest();
        request.setUserId(userId);
        request.setType(NotificationType.EMAIL);
        request.setPriority(NotificationPriority.HIGH);
        request.setSubject("Dispute Opened - Action Required");
        request.setBody("A dispute has been opened for escrow " + referenceId + ". Please respond within 48 hours.");
        request.setReferenceId(referenceId);
        request.setReferenceType("DISPUTE");
        request.setSourceEventId(sourceEventId);
        createNotification(request);
    }

    private void createDisputeResolvedNotification(String userId, String referenceId, String sourceEventId) {
        CreateNotificationRequest request = new CreateNotificationRequest();
        request.setUserId(userId);
        request.setType(NotificationType.EMAIL);
        request.setSubject("Dispute Resolved");
        request.setBody("The dispute for escrow " + referenceId + " has been resolved. Check your account for details.");
        request.setReferenceId(referenceId);
        request.setReferenceType("DISPUTE");
        request.setSourceEventId(sourceEventId);
        createNotification(request);
    }

    private NotificationResponse mapToResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .type(notification.getType())
                .status(notification.getStatus())
                .priority(notification.getPriority())
                .subject(notification.getSubject())
                .body(notification.getBody())
                .referenceId(notification.getReferenceId())
                .referenceType(notification.getReferenceType())
                .sentAt(notification.getSentAt())
                .deliveredAt(notification.getDeliveredAt())
                .readAt(notification.getReadAt())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
