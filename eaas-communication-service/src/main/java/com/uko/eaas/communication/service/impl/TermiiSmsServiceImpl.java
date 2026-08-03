package com.uko.eaas.communication.service.impl;

import com.uko.eaas.communication.service.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TermiiSmsServiceImpl implements SmsService {

    private final RestTemplate restTemplate;

    @Value("${sms.termii.api-key:}")
    private String apiKey;

    @Value("${sms.termii.sender-id:EaaS}")
    private String senderId;

    private static final String TERMII_API_URL = "https://api.ng.termii.com/api/sms/send";

    @Override
    public void sendSms(String phoneNumber, String message) {
        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("Termii API key not configured, SMS not sent to: {}", phoneNumber);
            return;
        }

        try {
            // Format phone number
            String formattedNumber = formatPhoneNumber(phoneNumber);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("to", formattedNumber);
            requestBody.put("from", senderId);
            requestBody.put("sms", message);
            requestBody.put("type", "plain");
            requestBody.put("channel", "generic");
            requestBody.put("api_key", apiKey);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(TERMII_API_URL, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("SMS sent successfully to: {}", phoneNumber);
            } else {
                log.error("Failed to send SMS: {}", response.getBody());
                throw new RuntimeException("SMS sending failed");
            }

        } catch (Exception e) {
            log.error("Failed to send SMS to {}: {}", phoneNumber, e.getMessage());
            throw new RuntimeException("SMS sending failed", e);
        }
    }

    @Override
    public boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            return false;
        }

        // Remove all non-digit characters
        String digits = phoneNumber.replaceAll("\\D", "");

        // Nigerian phone numbers should be 11 digits starting with 0, or 13 digits starting with 234
        return (digits.length() == 11 && digits.startsWith("0")) ||
               (digits.length() == 13 && digits.startsWith("234"));
    }

    private String formatPhoneNumber(String phoneNumber) {
        String digits = phoneNumber.replaceAll("\\D", "");

        // Convert 0XX to 234XX
        if (digits.length() == 11 && digits.startsWith("0")) {
            return "234" + digits.substring(1);
        }

        return digits;
    }
}
