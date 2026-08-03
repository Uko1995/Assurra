package com.uko.eaas.payment.client;

import com.uko.eaas.payment.dto.InitializePaymentRequest;
import com.uko.eaas.payment.dto.InitializePaymentResponse;
import com.uko.eaas.payment.dto.PaymentVerificationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class InterswitchClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${interswitch.api.url:https://sandbox.interswitchng.com}")
    private String apiUrl;

    @Value("${interswitch.api.key:}")
    private String apiKey;

    @Value("${interswitch.api.secret:}")
    private String apiSecret;

    @Value("${interswitch.mock.enabled:true}")
    private boolean mockEnabled;

    public InitializePaymentResponse initializePayment(InitializePaymentRequest request, String reference) {
        if (mockEnabled) {
            log.info("[MOCK] Initializing payment for reference: {}", reference);
            return InitializePaymentResponse.builder()
                    .reference(reference)
                    .escrowReference(request.getEscrowReference())
                    .paymentLink("https://mock-interswitch.com/pay/" + reference)
                    .amount(request.getAmount())
                    .currency(request.getCurrency())
                    .status("PENDING")
                    .build();
        }

        log.info("Initializing payment with Interswitch for reference: {}", reference);

        try {
            WebClient client = createWebClient();

            Map<String, Object> body = new HashMap<>();
            body.put("amount", request.getAmount().multiply(BigDecimal.valueOf(100)).intValue()); // Interswitch uses kobo
            body.put("currency", request.getCurrency());
            body.put("reference", reference);
            body.put("customerEmail", request.getCustomerEmail());
            body.put("callbackUrl", request.getCallbackUrl());

            Map<String, Object> response = client.post()
                    .uri("/api/v1/payments")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null) {
                throw new RuntimeException("Empty response from Interswitch");
            }

            return InitializePaymentResponse.builder()
                    .reference(reference)
                    .escrowReference(request.getEscrowReference())
                    .paymentLink((String) response.get("paymentLink"))
                    .amount(request.getAmount())
                    .currency(request.getCurrency())
                    .status("PENDING")
                    .build();

        } catch (WebClientResponseException e) {
            log.error("Interswitch payment initialization failed: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Payment initialization failed: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error initializing payment with Interswitch: {}", e.getMessage());
            throw new RuntimeException("Payment initialization failed", e);
        }
    }

    public PaymentVerificationResponse verifyPayment(String interswitchRef, BigDecimal expectedAmount) {
        if (mockEnabled) {
            log.info("[MOCK] Verifying payment: {}", interswitchRef);
            return PaymentVerificationResponse.builder()
                    .reference(interswitchRef)
                    .status("SUCCESS")
                    .amount(expectedAmount)
                    .channel("CARD")
                    .cardLast4("1234")
                    .cardBrand("VISA")
                    .bankName("Mock Bank")
                    .paidAt(LocalDateTime.now())
                    .build();
        }

        log.info("Verifying payment with Interswitch: {}", interswitchRef);

        try {
            WebClient client = createWebClient();

            Map<String, Object> response = client.get()
                    .uri("/api/v1/payments/{reference}", interswitchRef)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null) {
                throw new RuntimeException("Empty response from Interswitch");
            }

            return PaymentVerificationResponse.builder()
                    .reference((String) response.get("reference"))
                    .status((String) response.get("status"))
                    .amount(response.get("amount") != null
                            ? new BigDecimal(response.get("amount").toString()).divide(BigDecimal.valueOf(100))
                            : expectedAmount)
                    .channel((String) response.get("channel"))
                    .cardLast4((String) response.get("cardLast4"))
                    .cardBrand((String) response.get("cardBrand"))
                    .bankName((String) response.get("bankName"))
                    .paidAt(parseDateTime(response.get("paidAt")))
                    .build();

        } catch (WebClientResponseException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                log.warn("Payment not found in Interswitch: {}", interswitchRef);
                return null;
            }
            log.error("Interswitch payment verification failed: {}", e.getMessage());
            throw new RuntimeException("Payment verification failed", e);
        } catch (Exception e) {
            log.error("Error verifying payment with Interswitch: {}", e.getMessage());
            throw new RuntimeException("Payment verification failed", e);
        }
    }

    public String initiatePayout(String reference, BigDecimal amount, String bankCode, String accountNumber, String accountName) {
        if (mockEnabled) {
            log.info("[MOCK] Initiating payout: {} for amount {}", reference, amount);
            return "MOCK-TRANSFER-" + reference;
        }

        log.info("Initiating payout via Interswitch: {} for amount {}", reference, amount);

        try {
            WebClient client = createWebClient();

            Map<String, Object> body = new HashMap<>();
            body.put("amount", amount.multiply(BigDecimal.valueOf(100)).intValue());
            body.put("currency", "NGN");
            body.put("reference", reference);
            body.put("destinationBankCode", bankCode);
            body.put("destinationAccountNumber", accountNumber);
            body.put("destinationAccountName", accountName);
            body.put("narration", "EaaS Payout - " + reference);

            Map<String, Object> response = client.post()
                    .uri("/api/v1/transfers")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null) {
                throw new RuntimeException("Empty response from Interswitch");
            }

            return (String) response.get("transferReference");

        } catch (WebClientResponseException e) {
            log.error("Interswitch payout initiation failed: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Payout initiation failed: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error initiating payout with Interswitch: {}", e.getMessage());
            throw new RuntimeException("Payout initiation failed", e);
        }
    }

    public String initiateRefund(String paymentReference, BigDecimal amount) {
        if (mockEnabled) {
            log.info("[MOCK] Refunding payment: {} for amount {}", paymentReference, amount);
            return "MOCK-REFUND-" + paymentReference;
        }

        log.info("Refunding payment via Interswitch: {} for amount {}", paymentReference, amount);

        try {
            WebClient client = createWebClient();

            Map<String, Object> body = new HashMap<>();
            body.put("amount", amount.multiply(BigDecimal.valueOf(100)).intValue()); // Interswitch uses kobo
            body.put("currency", "NGN");
            body.put("reference", paymentReference);

            Map<String, Object> response = client.post()
                    .uri("/api/v1/payments/{reference}/refund", paymentReference)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null) {
                throw new RuntimeException("Empty response from Interswitch");
            }

            return (String) response.get("refundReference");

        } catch (WebClientResponseException e) {
            log.error("Interswitch refund failed: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Refund initiation failed: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error initiating refund with Interswitch: {}", e.getMessage());
            throw new RuntimeException("Refund initiation failed", e);
        }
    }

    public Map<String, Object> verifyPayout(String interswitchRef) {
        if (mockEnabled) {
            log.info("[MOCK] Verifying payout: {}", interswitchRef);
            Map<String, Object> response = new HashMap<>();
            response.put("transferReference", interswitchRef);
            response.put("status", "SUCCESS");
            response.put("amount", 100000);
            return response;
        }

        log.info("Verifying payout with Interswitch: {}", interswitchRef);

        try {
            WebClient client = createWebClient();

            return client.get()
                    .uri("/api/v1/transfers/{reference}", interswitchRef)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

        } catch (WebClientResponseException e) {
            log.error("Interswitch payout verification failed: {}", e.getMessage());
            throw new RuntimeException("Payout verification failed", e);
        } catch (Exception e) {
            log.error("Error verifying payout with Interswitch: {}", e.getMessage());
            throw new RuntimeException("Payout verification failed", e);
        }
    }

    public boolean verifyWebhookSignature(String payload, String signature) {
        if (mockEnabled) {
            log.debug("[MOCK] Verifying webhook signature (always returning true)");
            return true;
        }

        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKeySpec = new SecretKeySpec(apiSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            mac.init(secretKeySpec);

            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String computedSignature = Base64.getEncoder().encodeToString(hash);

            return computedSignature.equals(signature);
        } catch (Exception e) {
            log.error("Error verifying webhook signature: {}", e.getMessage());
            return false;
        }
    }

    private WebClient createWebClient() {
        return webClientBuilder
                .baseUrl(apiUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("Authorization", "Bearer " + generateAuthToken())
                .build();
    }

    private String generateAuthToken() {
        // In production, this would generate a proper OAuth token
        // For sandbox/testing, we use a simple Base64 encoding
        String credentials = apiKey + ":" + apiSecret;
        return Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    }

    private LocalDateTime parseDateTime(Object value) {
        if (value == null) return null;
        // Parse ISO date time string
        return LocalDateTime.parse(value.toString().replace("Z", ""));
    }
}
