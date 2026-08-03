package com.uko.eaas.payment.controller;

import com.uko.eaas.payment.dto.PaymentWebhookPayload;
import com.uko.eaas.payment.service.PaymentService;
import com.uko.eaas.payment.service.PayoutService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
public class WebhookController {

    private final PaymentService paymentService;
    private final PayoutService payoutService;

    @PostMapping("/interswitch/payments")
    public ResponseEntity<Void> handlePaymentWebhook(
            @RequestBody PaymentWebhookPayload payload,
            @RequestHeader("X-Interswitch-Signature") String signature) {

        log.info("Received payment webhook from Interswitch: {}", payload.getReference());

        try {
            paymentService.handleWebhook(payload, signature);
            return ResponseEntity.ok().build();
        } catch (SecurityException e) {
            log.error("Invalid webhook signature");
            return ResponseEntity.status(401).build();
        } catch (Exception e) {
            log.error("Error processing payment webhook: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/interswitch/payouts")
    public ResponseEntity<Void> handlePayoutWebhook(
            @RequestBody Map<String, Object> payload,
            @RequestHeader("X-Interswitch-Signature") String signature) {

        String interswitchRef = (String) payload.get("transferReference");
        String status = (String) payload.get("status");

        log.info("Received payout webhook from Interswitch: {} with status: {}", interswitchRef, status);

        try {
            payoutService.handlePayoutWebhook(interswitchRef, status);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error processing payout webhook: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "webhook-handler"));
    }
}
