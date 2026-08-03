package com.uko.eaas.identity.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class WebhookConfigResponse {

    private UUID merchantId;
    private String webhookUrl;
    private List<String> events;
    private String message;
}
