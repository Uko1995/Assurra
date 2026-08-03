package com.uko.eaas.communication.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uko.eaas.communication.client.MerchantWebhookClient;
import com.uko.eaas.communication.model.entity.WebhookEvent;
import com.uko.eaas.communication.repository.WebhookEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookEventPublisher {

    private final WebhookEventRepository webhookEventRepository;
    private final MerchantWebhookClient merchantWebhookClient;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public void publishIfConfigured(String eventType, String reference, UUID merchantId, Map<String, Object> payload) {
        try {
            MerchantWebhookClient.WebhookConfig config = merchantWebhookClient.getWebhookConfig(merchantId);
            if (config == null || config.getWebhookUrl() == null || config.getWebhookUrl().isBlank()) {
                return;
            }

            String payloadJson = objectMapper.writeValueAsString(payload);

            WebhookEvent event = WebhookEvent.builder()
                    .eventType(eventType)
                    .reference(reference)
                    .payload(payloadJson)
                    .targetUrl(config.getWebhookUrl())
                    .signature(config.getWebhookSecret())
                    .status("PENDING")
                    .attemptCount(0)
                    .nextAttemptAt(LocalDateTime.now())
                    .build();

            event = webhookEventRepository.save(event);

            rabbitTemplate.convertAndSend("webhook.direct", "webhook.delivery", event);
            log.info("Published webhook event: {} for merchant: {} to: {}", eventType, merchantId, config.getWebhookUrl());

        } catch (Exception e) {
            log.error("Failed to publish webhook event: {}", e.getMessage());
        }
    }
}
