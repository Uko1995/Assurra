package com.uko.eaas.communication.messaging;

import com.uko.eaas.communication.model.entity.WebhookEvent;
import com.uko.eaas.communication.service.WebhookDeliveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebhookDeliveryConsumer {

    private final WebhookDeliveryService webhookDeliveryService;

    @RabbitListener(queues = "webhook.delivery")
    public void consume(WebhookEvent event) {
        log.debug("Received webhook delivery task: {} attempt: {}", event.getReference(), event.getAttemptCount());
        webhookDeliveryService.deliver(event);
    }
}
