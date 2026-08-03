package com.uko.eaas.communication.client;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class MerchantWebhookClient {

    private final RestTemplate restTemplate;

    @Value("${identity.service.url:http://localhost:8081}")
    private String identityServiceUrl;

    public WebhookConfig getWebhookConfig(UUID merchantId) {
        try {
            String url = identityServiceUrl + "/internal/merchants/" + merchantId + "/webhook-config";
            return restTemplate.getForObject(url, WebhookConfig.class);
        } catch (Exception e) {
            log.warn("Failed to fetch webhook config for merchant {}: {}", merchantId, e.getMessage());
            return null;
        }
    }

    @Data
    public static class WebhookConfig {
        private String webhookUrl;
        private String webhookSecret;
    }
}
