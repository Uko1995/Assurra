package com.uko.eaas.escrow.config;

import com.uko.eaas.escrow.security.GatewayRequestValidationFilter;
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
 * Security configuration for Escrow Service.
 * 
 * This service relies on the API Gateway for authentication.
 * All requests must come through the gateway with proper signatures.
 * 
 * Security features:
 * - Request signature validation (from gateway)
 * - Stateless session (no server-side sessions)
 * - CSRF disabled (API service)
 * - Method-level security with @PreAuthorize
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
            // Disable CSRF for stateless API
            .csrf(AbstractHttpConfigurer::disable)
            
            // Stateless session management
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // Configure authorization
            .authorizeHttpRequests(auth -> auth
                // Public health endpoints (no signature required)
                .requestMatchers("/actuator/health", "/health", "/api/v1/escrow/health").permitAll()
                
                // Internal service-to-service endpoints
                .requestMatchers("/internal/**").permitAll()
                
                // All other requests require gateway signature
                .anyRequest().authenticated()
            )
            
            // Disable default authentication mechanisms
            .httpBasic(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .logout(AbstractHttpConfigurer::disable)
            
            // Add our custom gateway validation filter
            .addFilterBefore(gatewayRequestValidationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
