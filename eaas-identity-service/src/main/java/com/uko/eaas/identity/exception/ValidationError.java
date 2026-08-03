package com.uko.eaas.identity.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a validation error for a specific field.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidationError {
    
    private String field;
    private String message;
    private String rejectedValue;
}
