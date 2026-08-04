package com.uko.eaas.communication.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class IdentityServiceClient {

    private final RestTemplate restTemplate;

    @Value("${identity.service.url:http://localhost:8081}")
    private String identityServiceUrl;

    public Optional<UserContact> getUserContact(String userId) {
        String url = identityServiceUrl + "/internal/users/" + userId + "/contact";
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            ResponseEntity<UserContact> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    UserContact.class);

            UserContact contact = response.getBody();
            if (contact == null || (contact.getEmail() == null && contact.getPhoneNumber() == null)) {
                log.warn("Identity service returned no contact details for user: {}", userId);
                return Optional.empty();
            }
            return Optional.of(contact);
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("User not found in identity service: {}", userId);
            return Optional.empty();
        } catch (RestClientException e) {
            log.error("Failed to fetch contact details for user {}: {}", userId, e.getMessage());
            return Optional.empty();
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserContact {
        private String email;
        private String phoneNumber;
    }
}