package com.uko.eaas.identity.exception;

import org.springframework.http.HttpStatus;

public class ValidationException extends BusinessException {
    
    public ValidationException(String message) {
        super(message, "VALIDATION_ERROR", HttpStatus.BAD_REQUEST);
    }
}
