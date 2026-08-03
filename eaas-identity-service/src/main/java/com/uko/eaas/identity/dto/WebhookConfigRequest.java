package com.uko.eaas.identity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.List;

@Data
public class WebhookConfigRequest {

    @NotBlank
    @Pattern(regexp = "^https://.*", message = "Webhook URL must use HTTPS")
    private String url;

    private List<String> events;
}
