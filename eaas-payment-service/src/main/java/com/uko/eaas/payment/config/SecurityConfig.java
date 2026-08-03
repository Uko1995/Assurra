package com.uko.eaas.payment.config;

import com.uko.eaas.payment.security.GatewayRequestValidationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security configuration for Payment Service.
 * 
 * This service handles financial transactions and requires:
 * - Strict gateway signature validation
 * - Webhook endpoints that use HMAC verification (separate from gateway auth)
 * - Method-level security for sensitive operations
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final GatewayRequestValidationFilter gatewayRequestValidationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public health endpoints
                .requestMatchers("/actuator/health", "/health", "/api/v1/payments/health").permitAll()
                
                // Error dispatch path — must be public so validation errors get a proper response
                .requestMatchers("/error").permitAll()
                
                // Webhooks from payment providers (use their own HMAC verification)
                .requestMatchers("/api/v1/webhooks/**").permitAll()
                
                // All other payment endpoints require gateway signature
                .anyRequest().authenticated()
            )
            .httpBasic(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .logout(AbstractHttpConfigurer::disable)
            .addFilterBefore(gatewayRequestValidationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
