package com.uko.eaas.escrow.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uko.eaas.escrow.dto.PaymentWebhookRequest;
import com.uko.eaas.escrow.service.EscrowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final EscrowService escrowService;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = "escrow.payment.events")
    @Transactional
    public void onMessage(Message message) throws Exception {
        try {
            String json = new String(message.getBody());
            JsonNode root = objectMapper.readTree(json);
            String eventType = root.path("eventType").asText("");

            if (!eventType.startsWith("payment.")) {
                log.debug("Ignoring non-payment event on escrow.payment.events: {}", eventType);
                return;
            }

            String escrowReference = root.path("escrowReference").asText();
            if (escrowReference.isBlank()) {
                log.error("Received payment event without escrowReference: {}", eventType);
                return;
            }

            PaymentWebhookRequest request = new PaymentWebhookRequest();
            request.setReference(escrowReference);
            request.setPaymentReference(root.path("reference").asText(null));
            request.setStatus(root.path("status").asText(""));
            if (root.hasNonNull("amount")) {
                request.setAmount(root.path("amount").decimalValue());
            }
            request.setChannel(root.path("channel").asText(null));
            if (root.hasNonNull("timestamp")) {
                request.setPaidAt(LocalDateTime.parse(root.path("timestamp").asText()));
            }

            escrowService.handlePaymentWebhook(request);
        } catch (Exception e) {
            log.error("Failed to process payment event: {}", e.getMessage(), e);
            throw e;
        }
    }
}
