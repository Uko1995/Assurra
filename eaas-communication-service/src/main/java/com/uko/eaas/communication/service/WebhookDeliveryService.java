package com.uko.eaas.communication.service;

import com.uko.eaas.communication.config.WebhookRetryConfig;
import com.uko.eaas.communication.model.entity.WebhookEvent;
import com.uko.eaas.communication.repository.WebhookEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookDeliveryService {

    private final WebhookEventRepository webhookEventRepository;
    private final RabbitTemplate rabbitTemplate;
    private final WebClient.Builder webClientBuilder;

    public void deliver(WebhookEvent event) {
        log.info("Delivering webhook: {} to {}", event.getReference(), event.getTargetUrl());

        try {
            String signature = signPayload(event.getPayload(), event.getSignature());

            ResponseEntity<String> response = webClientBuilder.build()
                    .post()
                    .uri(event.getTargetUrl())
                    .header("X-EaaS-Signature", signature)
                    .header("X-EaaS-Event", event.getEventType())
                    .header("X-EaaS-Reference", event.getReference())
                    .header("Content-Type", "application/json")
                    .bodyValue(event.getPayload())
                    .retrieve()
                    .toEntity(String.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();

            event.setStatus("DELIVERED");
            event.setHttpStatus(response != null ? response.getStatusCodeValue() : 200);
            event.setResponseBody(response != null ? response.getBody() : null);
            event.setDeliveredAt(LocalDateTime.now());
            event.setAttemptCount(event.getAttemptCount() + 1);
            event.setLastAttemptAt(LocalDateTime.now());
            webhookEventRepository.save(event);

            log.info("Webhook delivered successfully: {} status: {}", event.getReference(), event.getHttpStatus());

        } catch (Exception e) {
            log.error("Webhook delivery failed: {} error: {}", event.getReference(), e.getMessage());
            handleFailure(event, e.getMessage());
        }
    }

    private void handleFailure(WebhookEvent event, String error) {
        int attemptCount = event.getAttemptCount() + 1;
        event.setAttemptCount(attemptCount);
        event.setLastAttemptAt(LocalDateTime.now());
        event.setLastError(error);

        if (attemptCount >= WebhookRetryConfig.MAX_RETRIES) {
            event.setStatus("FAILED");
            webhookEventRepository.save(event);
            rabbitTemplate.convertAndSend(WebhookRetryConfig.WEBHOOK_DLX, WebhookRetryConfig.WEBHOOK_DLQ, event);
            log.error("Webhook {} failed permanently after {} attempts", event.getReference(), WebhookRetryConfig.MAX_RETRIES);
            return;
        }

        int delayMinutes = WebhookRetryConfig.RETRY_DELAYS_MINUTES[attemptCount - 1];
        long delayMs = delayMinutes * 60 * 1000L;

        event.setStatus("PENDING");
        event.setNextAttemptAt(LocalDateTime.now().plusMinutes(delayMinutes));
        webhookEventRepository.save(event);

        // Publish to retry queue with TTL
        rabbitTemplate.convertAndSend(WebhookRetryConfig.WEBHOOK_EXCHANGE, WebhookRetryConfig.WEBHOOK_RETRY_QUEUE, event, msg -> {
            msg.getMessageProperties().setExpiration(String.valueOf(delayMs));
            return msg;
        });

        log.info("Webhook {} scheduled for retry {} in {} minutes", event.getReference(), attemptCount, delayMinutes);
    }

    private String signPayload(String payload, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] signatureBytes = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(signatureBytes);
        } catch (Exception e) {
            throw new RuntimeException("Failed to sign webhook payload", e);
        }
    }
}
