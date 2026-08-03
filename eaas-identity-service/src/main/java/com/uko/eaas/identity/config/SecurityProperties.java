package com.uko.eaas.identity.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Security configuration properties.
 * Configure in application.yml:
 * <pre>
 * security:
 *   login:
 *     max-failed-attempts: 5
 *     lockout-minutes: 30
 * </pre>
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "security.login")
public class SecurityProperties {

    /**
     * Maximum number of failed login attempts before account lockout.
     * Default: 5
     */
    private int maxFailedAttempts = 5;

    /**
     * Account lockout duration in minutes after max failed attempts.
     * Default: 30 minutes
     */
    private int lockoutMinutes = 30;
}
