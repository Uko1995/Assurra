package com.uko.eaas.communication.config;

import com.uko.eaas.communication.security.GatewayRequestValidationFilter;
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
 * Security configuration for Communication Service.
 * 
 * This service handles notifications and disputes and requires:
 * - Gateway signature validation for all user-facing endpoints
 * - RabbitMQ message consumers run internally (no HTTP security needed)
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
                .requestMatchers("/actuator/health", "/health", "/api/v1/health").permitAll()
                
                // Error dispatch path — defensive: ensures unhandled exceptions get a response
                .requestMatchers("/error").permitAll()
                
                // All other endpoints require gateway signature
                .anyRequest().authenticated()
            )
            .httpBasic(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .logout(AbstractHttpConfigurer::disable)
            .addFilterBefore(gatewayRequestValidationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
