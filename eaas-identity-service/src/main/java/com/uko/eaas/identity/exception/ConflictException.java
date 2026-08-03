package com.uko.eaas.identity.exception;

import org.springframework.http.HttpStatus;

public class ConflictException extends BusinessException {
    
    public ConflictException(String message) {
        super(message, "CONFLICT", HttpStatus.CONFLICT);
    }
}
