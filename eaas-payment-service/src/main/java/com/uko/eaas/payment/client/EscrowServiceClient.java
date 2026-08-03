package com.uko.eaas.payment.client;

import com.uko.eaas.payment.dto.ApiResponse;
import com.uko.eaas.payment.dto.EscrowData;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class EscrowServiceClient {

    private final RestTemplate restTemplate;

    @Value("${escrow.service.url:http://localhost:8082}")
    private String escrowServiceUrl;

    public EscrowData getEscrow(String reference) {
        String url = escrowServiceUrl + "/internal/api/v1/escrow/" + reference;
        log.debug("Fetching escrow from escrow service: {}", reference);

        try {
            ResponseEntity<ApiResponse<EscrowData>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<ApiResponse<EscrowData>>() {}
            );

            ApiResponse<EscrowData> body = response.getBody();
            if (body == null || !body.isSuccess() || body.getData() == null) {
                throw new IllegalStateException("Failed to fetch escrow: " + reference);
            }

            return body.getData();
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Escrow not found: {}", reference);
            throw new EntityNotFoundException("Escrow not found: " + reference);
        } catch (RestClientException e) {
            log.error("Failed to fetch escrow {}: {}", reference, e.getMessage());
            throw new IllegalStateException("Unable to retrieve escrow details", e);
        }
    }
}
