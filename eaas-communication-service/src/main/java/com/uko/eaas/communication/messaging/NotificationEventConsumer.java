package com.uko.eaas.communication.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uko.eaas.communication.messaging.event.EscrowEvent;
import com.uko.eaas.communication.messaging.event.PaymentEvent;
import com.uko.eaas.communication.messaging.event.PayoutEvent;
import com.uko.eaas.communication.model.entity.Dispute;
import com.uko.eaas.communication.model.entity.DisputeMessage;
import com.uko.eaas.communication.model.entity.Notification;
import com.uko.eaas.communication.repository.DisputeMessageRepository;
import com.uko.eaas.communication.repository.DisputeRepository;
import com.uko.eaas.communication.repository.NotificationRepository;
import com.uko.eaas.communication.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventConsumer {

    private static final UUID ANONYMIZED_SENTINEL = UUID.fromString("00000000-0000-0000-0000-000000000000");
    private static final int BATCH_SIZE = 200;

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;
    private final com.uko.eaas.communication.service.WebhookEventPublisher webhookEventPublisher;
    private final NotificationRepository notificationRepository;
    private final DisputeRepository disputeRepository;
    private final DisputeMessageRepository disputeMessageRepository;

    @RabbitListener(queues = "comm.notifications")
    public void onMessage(Message message) throws Exception {
        String json = new String(message.getBody());
        JsonNode root = objectMapper.readTree(json);
        String eventType = root.path("eventType").asText("");

        log.info("Received event: {} on comm.notifications queue", eventType);

        try {
            if (eventType.startsWith("escrow.")) {
                EscrowEvent event = objectMapper.treeToValue(root, EscrowEvent.class);
                handleEscrowEvent(event);
            } else if (eventType.startsWith("payment.")) {
                PaymentEvent event = objectMapper.treeToValue(root, PaymentEvent.class);
                handlePaymentEvent(event);
            } else if (eventType.startsWith("payout.")) {
                PayoutEvent event = objectMapper.treeToValue(root, PayoutEvent.class);
                handlePayoutEvent(event);
            } else if ("user.anonymized".equals(eventType)) {
                handleUserAnonymizedEvent(root);
            } else {
                log.warn("Unknown event type received: {}", eventType);
            }
        } catch (Exception e) {
            log.error("Failed to process event {}: {}", eventType, e.getMessage(), e);
            throw e; // Let Spring retry / DLQ handle it
        }
    }

    private void handleEscrowEvent(EscrowEvent event) {
        String eventType = event.getEventType();
        String sourceEventId = eventType + ":" + event.getReference();
        Map<String, Object> data = nullSafeMap(
                "reference", event.getReference(),
                "amount", event.getAmount(),
                "currency", event.getCurrency(),
                "status", event.getStatus()
        );

        switch (eventType) {
            case "escrow.created" ->
                    notificationService.sendNotificationForEvent(eventType, event.getCustomerId(), event.getReference(), data, sourceEventId + ":cust");
            case "escrow.funded" -> {
                notificationService.sendNotificationForEvent(eventType, event.getCustomerId(), event.getReference(), data, sourceEventId + ":cust");
                notificationService.sendNotificationForEvent(eventType, event.getMerchantId(), event.getReference(), data, sourceEventId + ":merch");
                webhookEventPublisher.publishIfConfigured(eventType, event.getReference(), safeParseUUID(event.getMerchantId()), data);
            }
            case "escrow.shipped" ->
                    notificationService.sendNotificationForEvent(eventType, event.getCustomerId(), event.getReference(), data, sourceEventId + ":cust");
            case "escrow.delivered" -> {
                notificationService.sendNotificationForEvent(eventType, event.getCustomerId(), event.getReference(), data, sourceEventId + ":cust");
                webhookEventPublisher.publishIfConfigured(eventType, event.getReference(), safeParseUUID(event.getMerchantId()), data);
            }
            case "escrow.confirmed" -> {
                notificationService.sendNotificationForEvent(eventType, event.getMerchantId(), event.getReference(), data, sourceEventId + ":merch");
                webhookEventPublisher.publishIfConfigured(eventType, event.getReference(), safeParseUUID(event.getMerchantId()), data);
            }
            case "escrow.auto-released" -> {
                notificationService.sendNotificationForEvent(eventType, event.getCustomerId(), event.getReference(), data, sourceEventId + ":cust");
                notificationService.sendNotificationForEvent(eventType, event.getMerchantId(), event.getReference(), data, sourceEventId + ":merch");
                webhookEventPublisher.publishIfConfigured(eventType, event.getReference(), safeParseUUID(event.getMerchantId()), data);
            }
            case "escrow.cancelled.refund" ->
                    notificationService.sendNotificationForEvent(eventType, event.getCustomerId(), event.getReference(), data, sourceEventId + ":cust");
            case "escrow.expired" ->
                    notificationService.sendNotificationForEvent(eventType, event.getCustomerId(), event.getReference(), data, sourceEventId + ":cust");
            default -> log.debug("No notification mapping for escrow event: {}", eventType);
        }
    }

    private void handlePaymentEvent(PaymentEvent event) {
        String eventType = event.getEventType();
        String sourceEventId = eventType + ":" + event.getReference();
        Map<String, Object> data = nullSafeMap(
                "reference", event.getReference(),
                "escrowReference", event.getEscrowReference(),
                "amount", event.getAmount(),
                "status", event.getStatus()
        );

        if ("payment.refunded".equals(eventType)) {
            notificationService.sendNotificationForEvent(eventType, event.getCustomerId(), event.getReference(), data, sourceEventId);
        } else {
            log.debug("No notification mapping for payment event: {}", eventType);
        }
    }

    private void handlePayoutEvent(PayoutEvent event) {
        String eventType = event.getEventType();
        String sourceEventId = eventType + ":" + event.getReference();
        Map<String, Object> data = nullSafeMap(
                "reference", event.getReference(),
                "escrowReference", event.getEscrowReference(),
                "amount", event.getNetAmount(),
                "status", event.getStatus()
        );

        if ("payout.completed".equals(eventType) || "payout.failed".equals(eventType)) {
            notificationService.sendNotificationForEvent(eventType, event.getMerchantId(), event.getReference(), data, sourceEventId);
        } else {
            log.debug("No notification mapping for payout event: {}", eventType);
        }
    }

    private Map<String, Object> nullSafeMap(Object... keyValues) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i + 1 < keyValues.length; i += 2) {
            if (keyValues[i] instanceof String key && keyValues[i + 1] != null) {
                map.put(key, keyValues[i + 1]);
            }
        }
        return map;
    }

    private UUID safeParseUUID(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Transactional
    private void handleUserAnonymizedEvent(JsonNode root) {
        String userIdStr = root.path("userId").asText();
        if (userIdStr.isBlank()) return;

        UUID userId = UUID.fromString(userIdStr);
        log.info("Anonymizing communication data for userId={}", userId);

        anonymizeNotifications(userId);
        anonymizeDisputes(userId);
        anonymizeDisputeMessages(userId);

        log.info("Successfully anonymized communication data for userId={}", userId);
    }

    private void anonymizeNotifications(UUID userId) {
        Page<Notification> notifications = notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, BATCH_SIZE));
        int count = 0;
        for (Notification n : notifications) {
            n.setUserId(ANONYMIZED_SENTINEL);
            notificationRepository.save(n);
            count++;
        }
        if (count > 0) {
            log.info("Anonymized {} notifications for userId={}", count, userId);
        }
    }

    private void anonymizeDisputes(UUID userId) {
        Page<Dispute> byCustomer = disputeRepository
                .findByCustomerIdOrderByOpenedAtDesc(userId, PageRequest.of(0, BATCH_SIZE));
        int count = 0;
        for (Dispute d : byCustomer) {
            if (userId.equals(d.getCustomerId())) d.setCustomerId(ANONYMIZED_SENTINEL);
            if (userId.equals(d.getRaisedBy())) d.setRaisedBy(ANONYMIZED_SENTINEL);
            if (userId.equals(d.getResolvedBy())) d.setResolvedBy(ANONYMIZED_SENTINEL);
            disputeRepository.save(d);
            count++;
        }

        Page<Dispute> byMerchant = disputeRepository
                .findByMerchantIdOrderByOpenedAtDesc(userId, PageRequest.of(0, BATCH_SIZE));
        for (Dispute d : byMerchant) {
            if (userId.equals(d.getMerchantId())) d.setMerchantId(ANONYMIZED_SENTINEL);
            if (userId.equals(d.getRaisedBy())) d.setRaisedBy(ANONYMIZED_SENTINEL);
            if (userId.equals(d.getResolvedBy())) d.setResolvedBy(ANONYMIZED_SENTINEL);
            disputeRepository.save(d);
            count++;
        }

        if (count > 0) {
            log.info("Anonymized {} disputes for userId={}", count, userId);
        }
    }

    private void anonymizeDisputeMessages(UUID userId) {
        List<DisputeMessage> allMessages = disputeMessageRepository.findAll();
        int count = 0;
        for (DisputeMessage msg : allMessages) {
            if (userId.equals(msg.getSenderId())) {
                msg.setSenderId(ANONYMIZED_SENTINEL);
                disputeMessageRepository.save(msg);
                count++;
            }
        }
        if (count > 0) {
            log.info("Anonymized {} dispute messages for userId={}", count, userId);
        }
    }
}
