package com.uko.eaas.identity.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when the authenticated user does not have the required role.
 * Returns HTTP 403 Forbidden.
 */
public class ForbiddenException extends BusinessException {

    public ForbiddenException(String message) {
        super(message, "FORBIDDEN", HttpStatus.FORBIDDEN);
    }

    public ForbiddenException(String expectedRole, String actualRole) {
        super(
            String.format("Access denied. This endpoint requires %s role. Your role is %s.", expectedRole, actualRole),
            "FORBIDDEN",
            HttpStatus.FORBIDDEN
        );
    }
}
