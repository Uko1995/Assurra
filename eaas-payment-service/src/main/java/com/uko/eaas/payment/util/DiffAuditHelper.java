package com.uko.eaas.payment.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uko.eaas.payment.messaging.event.AuditEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class DiffAuditHelper {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static Map<String, AuditEvent.FieldChange> diff(Object oldObj, Object newObj) {
        Map<String, AuditEvent.FieldChange> changes = new HashMap<>();
        if (oldObj == null || newObj == null) {
            return changes;
        }

        try {
            Map<String, Object> oldMap = objectMapper.convertValue(oldObj, Map.class);
            Map<String, Object> newMap = objectMapper.convertValue(newObj, Map.class);

            for (String key : newMap.keySet()) {
                Object oldValue = oldMap.get(key);
                Object newValue = newMap.get(key);

                if (!isEqual(oldValue, newValue)) {
                    changes.put(key, new AuditEvent.FieldChange(oldValue, newValue));
                }
            }
        } catch (Exception e) {
            log.error("Failed to compute diff: {}", e.getMessage());
        }

        return changes;
    }

    private static boolean isEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }
}
