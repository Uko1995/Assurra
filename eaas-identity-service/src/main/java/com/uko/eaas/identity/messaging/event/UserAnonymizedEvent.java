package com.uko.eaas.identity.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAnonymizedEvent implements Serializable {

    private String eventType = "user.anonymized";
    private UUID userId;
    private String userRole;
    private Instant timestamp;
}