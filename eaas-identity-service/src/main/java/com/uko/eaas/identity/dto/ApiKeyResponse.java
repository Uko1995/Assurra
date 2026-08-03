package com.uko.eaas.identity.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ApiKeyResponse {
    private String message;
    private String apiKey;
    private String prefix;
    private LocalDateTime createdAt;
    private String warning;
}
