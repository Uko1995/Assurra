package com.uko.eaas.identity.messaging.event;

import com.uko.eaas.identity.model.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditEvent implements Serializable {

    private String eventType;
    private String entityType;
    private String entityId;
    private String action;
    private UUID performedBy;
    private UserRole performedByRole;
    private Map<String, FieldChange> changes;
    private String metadata;
    private String ipAddress;
    private String userAgent;
    private Instant timestamp;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FieldChange implements Serializable {
        private Object oldValue;
        private Object newValue;
    }
}
