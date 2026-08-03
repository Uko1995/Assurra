package com.uko.eaas.communication.client;

import com.uko.eaas.communication.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class EscrowServiceClient {

    private final RestTemplate restTemplate;

    @Value("${escrow.service.url:http://localhost:8082}")
    private String escrowServiceUrl;

    public void markDispute(String escrowReference) {
        String url = escrowServiceUrl + "/internal/api/v1/escrow/" + escrowReference + "/dispute";
        log.debug("Marking escrow as disputed: {}", escrowReference);
        postForStatus(url, null, escrowReference);
    }

    public void resolveDispute(String escrowReference, String resolution) {
        String url = escrowServiceUrl + "/internal/api/v1/escrow/" + escrowReference + "/dispute/resolve";
        log.debug("Resolving escrow dispute for {} with resolution: {}", escrowReference, resolution);
        postForStatus(url, Map.of("resolution", resolution), escrowReference);
    }

    private void postForStatus(String url, Object body, String escrowReference) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            ResponseEntity<ApiResponse<Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    new HttpEntity<>(body, headers),
                    new ParameterizedTypeReference<ApiResponse<Object>>() {
                    });

            ApiResponse<Object> api = response.getBody();
            if (api == null || !api.isSuccess()) {
                throw new IllegalStateException(api != null && api.getMessage() != null
                        ? api.getMessage()
                        : "Escrow service returned an empty response");
            }
        } catch (HttpClientErrorException e) {
            log.warn("Escrow service rejected request for {}: {}", escrowReference, e.getResponseBodyAsString());
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new IllegalStateException("Escrow not found: " + escrowReference, e);
            }
            throw new IllegalStateException("Escrow service rejected the request for " + escrowReference, e);
        } catch (RestClientException e) {
            log.error("Failed to call escrow service for {}: {}", escrowReference, e.getMessage());
            throw new IllegalStateException("Unable to update escrow status in escrow service", e);
        }
    }
}