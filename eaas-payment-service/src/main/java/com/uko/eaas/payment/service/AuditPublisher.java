package com.uko.eaas.payment.service;

import com.uko.eaas.payment.messaging.event.AuditEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publish(AuditEvent event) {
        try {
            rabbitTemplate.convertAndSend("eaas.exchange", "audit.event", event);
            log.debug("Published audit event: {} for entity: {}", event.getEventType(), event.getEntityId());
        } catch (Exception e) {
            log.error("Failed to publish audit event: {}", e.getMessage());
        }
    }
}
