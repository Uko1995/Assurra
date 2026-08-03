package com.uko.eaas.payment.client;

import com.uko.eaas.payment.dto.MerchantSettlementDetailsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class MerchantInternalClient {

    private final RestTemplate restTemplate;

    @Value("${identity.service.url:http://localhost:8081}")
    private String identityServiceUrl;

    public MerchantSettlementDetailsResponse getSettlementDetails(String merchantId) {
        String url = identityServiceUrl + "/internal/merchants/" + merchantId + "/settlement-details";
        log.debug("Fetching settlement details from Identity Service for merchant: {}", merchantId);

        try {
            ResponseEntity<MerchantSettlementDetailsResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {}
            );

            if (response.getBody() == null) {
                throw new IllegalStateException("Empty response from Identity Service for merchant: " + merchantId);
            }

            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to fetch settlement details for merchant {}: {}", merchantId, e.getMessage());
            throw new IllegalStateException("Unable to retrieve merchant settlement details", e);
        }
    }
}
