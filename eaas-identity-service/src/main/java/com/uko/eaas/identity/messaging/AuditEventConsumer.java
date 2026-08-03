package com.uko.eaas.identity.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uko.eaas.identity.messaging.event.AuditEvent;
import com.uko.eaas.identity.messaging.event.EscrowEvent;
import com.uko.eaas.identity.messaging.event.PaymentEvent;
import com.uko.eaas.identity.messaging.event.PayoutEvent;
import com.uko.eaas.identity.model.entity.AuditLog;
import com.uko.eaas.identity.model.enums.UserRole;
import com.uko.eaas.identity.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuditEventConsumer {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = "identity.audit.events")
    @Transactional
    public void onMessage(Message message) throws Exception {
        String json = new String(message.getBody());
        JsonNode root = objectMapper.readTree(json);
        String eventType = root.path("eventType").asText("");

        log.info("Received audit event: {} on identity.audit.events queue", eventType);

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
            } else if (eventType.startsWith("audit.")) {
                AuditEvent event = objectMapper.treeToValue(root, AuditEvent.class);
                handleAuditEvent(event);
            } else {
                log.warn("Unknown event type received for audit: {}", eventType);
            }
        } catch (Exception e) {
            log.error("Failed to process audit event {}: {}", eventType, e.getMessage(), e);
            throw e;
        }
    }

    private void handleAuditEvent(AuditEvent event) {
        UUID entityId = safeParseUUID(event.getEntityId());

        String changesJson = null;
        if (event.getChanges() != null && !event.getChanges().isEmpty()) {
            try {
                changesJson = objectMapper.writeValueAsString(event.getChanges());
            } catch (Exception e) {
                log.warn("Failed to serialize changes: {}", e.getMessage());
            }
        }

        AuditLog auditLog = AuditLog.builder()
                .entityType(event.getEntityType())
                .entityId(entityId != null ? entityId : new UUID(0, 0))
                .action(event.getAction())
                .performedBy(event.getPerformedBy())
                .performedByRole(event.getPerformedByRole() != null ? event.getPerformedByRole() : UserRole.SYSTEM)
                .oldValues(changesJson)
                .metadata(event.getMetadata())
                .ipAddress(event.getIpAddress())
                .userAgent(event.getUserAgent())
                .build();

        auditLogRepository.save(auditLog);
        log.debug("Audit log saved for event: {} entity: {} action: {}",
                event.getEventType(), event.getEntityId(), event.getAction());
    }

    private void handleEscrowEvent(EscrowEvent event) {
        UUID entityId = safeParseUUID(event.getEscrowId());

        UserRole performedByRole = null;
        UUID performedBy = null;
        if (event.getTriggeredBy() != null) {
            try {
                performedByRole = UserRole.valueOf(event.getTriggeredBy());
            } catch (IllegalArgumentException e) {
                log.warn("Unknown triggeredBy value: {}", event.getTriggeredBy());
            }
        }
        if (performedByRole == UserRole.CUSTOMER) {
            performedBy = safeParseUUID(event.getCustomerId());
        } else if (performedByRole == UserRole.MERCHANT) {
            performedBy = safeParseUUID(event.getMerchantId());
        }

        AuditLog auditLog = AuditLog.builder()
                .entityType("ESCROW")
                .entityId(entityId != null ? entityId : new UUID(0, 0))
                .action(event.getEventType().toUpperCase().replace(".", "_"))
                .performedBy(performedBy)
                .performedByRole(performedByRole)
                .metadata(String.format(
                        "{\"reference\": \"%s\", \"amount\": \"%s\", \"status\": \"%s\", \"merchantId\": \"%s\"}",
                        event.getReference(),
                        event.getAmount(),
                        event.getStatus(),
                        event.getMerchantId()
                ))
                .build();

        auditLogRepository.save(auditLog);
        log.debug("Audit log saved for escrow event: {} reference: {}", event.getEventType(), event.getReference());
    }

    private void handlePaymentEvent(PaymentEvent event) {
        UUID entityId = safeParseUUID(event.getReference());
        UUID performedBy = safeParseUUID(event.getCustomerId());

        AuditLog auditLog = AuditLog.builder()
                .entityType("PAYMENT")
                .entityId(entityId != null ? entityId : new UUID(0, 0))
                .action(event.getEventType().toUpperCase().replace(".", "_"))
                .performedBy(performedBy)
                .metadata(String.format(
                        "{\"reference\": \"%s\", \"escrowReference\": \"%s\", \"amount\": \"%s\", \"status\": \"%s\"}",
                        event.getReference(),
                        event.getEscrowReference(),
                        event.getAmount(),
                        event.getStatus()
                ))
                .build();

        auditLogRepository.save(auditLog);
        log.debug("Audit log saved for payment event: {} reference: {}", event.getEventType(), event.getReference());
    }

    private void handlePayoutEvent(PayoutEvent event) {
        UUID entityId = safeParseUUID(event.getReference());
        UUID performedBy = safeParseUUID(event.getMerchantId());

        AuditLog auditLog = AuditLog.builder()
                .entityType("PAYOUT")
                .entityId(entityId != null ? entityId : new UUID(0, 0))
                .action(event.getEventType().toUpperCase().replace(".", "_"))
                .performedBy(performedBy)
                .metadata(String.format(
                        "{\"reference\": \"%s\", \"escrowReference\": \"%s\", \"amount\": \"%s\", \"status\": \"%s\"}",
                        event.getReference(),
                        event.getEscrowReference(),
                        event.getAmount(),
                        event.getStatus()
                ))
                .build();

        auditLogRepository.save(auditLog);
        log.debug("Audit log saved for payout event: {} reference: {}", event.getEventType(), event.getReference());
    }

    private UUID safeParseUUID(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid UUID format: {}", value);
            return null;
        }
    }
}
