package com.uko.eaas.payment.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

/**
 * Filter to validate requests that come from the API Gateway.
 * 
 * This filter verifies that:
 * 1. The request has a valid signature from the gateway
 * 2. The request timestamp is recent (prevents replay attacks)
 * 3. The required headers are present
 * 
 * NOTE: Webhook endpoints are excluded as they use provider-specific HMAC verification.
 */
@Slf4j
@Component
public class GatewayRequestValidationFilter extends OncePerRequestFilter {

    @Value("${gateway.internal.signing-secret:internal-gateway-signing-secret-change-in-production}")
    private String gatewaySigningSecret;
    
    private static final long MAX_REQUEST_AGE_SECONDS = 300;
    
    private static final List<String> PUBLIC_PATHS = List.of(
        "/actuator/health",
        "/health",
        "/api/v1/payments/health",
        // Webhooks are handled by separate HMAC verification
        "/api/v1/webhooks/",
        // Error dispatch path (must be public so validation errors get proper responses)
        "/error"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        String path = request.getRequestURI();
        
        if (isPublicPath(path)) {
            filterChain.doFilter(request, response);
            return;
        }
        
        try {
            String userId = request.getHeader("X-User-Id");
            String role = request.getHeader("X-User-Role");
            String timestamp = request.getHeader("X-Request-Timestamp");
            String nonce = request.getHeader("X-Request-Nonce");
            String signature = request.getHeader("X-Request-Signature");
            
            if (userId == null || timestamp == null || nonce == null || signature == null) {
                log.warn("Missing required security headers for path: {}", path);
                sendError(response, HttpServletResponse.SC_UNAUTHORIZED, 
                        "Missing required security headers");
                return;
            }
            
            if (!isRequestTimestampValid(timestamp)) {
                log.warn("Request timestamp too old: {}", timestamp);
                sendError(response, HttpServletResponse.SC_UNAUTHORIZED, 
                        "Request expired");
                return;
            }
            
            if (!validateSignature(request, userId, role, timestamp, nonce, signature)) {
                log.warn("Invalid signature for path: {}", path);
                sendError(response, HttpServletResponse.SC_UNAUTHORIZED, 
                        "Invalid request signature");
                return;
            }
            
            List<SimpleGrantedAuthority> authorities = role != null 
                    ? List.of(new SimpleGrantedAuthority("ROLE_" + role))
                    : Collections.emptyList();
            
            UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(userId, null, authorities);
            
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            log.debug("Request validated for user: {} with role: {}", userId, role);
            
            filterChain.doFilter(request, response);
            
        } catch (Exception e) {
            log.error("Error validating request", e);
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                    "Authentication error");
        }
    }
    
    private boolean validateSignature(HttpServletRequest request, String userId, String role,
                                     String timestamp, String nonce, String signature) {
        try {
            String method = request.getMethod();
            String path = request.getRequestURI();
            
            String signatureData = String.join("|",
                    method,
                    path,
                    userId != null ? userId : "",
                    role != null ? role : "",
                    timestamp,
                    nonce
            );
            
            String expectedSignature = generateHmac(signatureData, gatewaySigningSecret);
            return constantTimeEquals(signature, expectedSignature);
            
        } catch (Exception e) {
            log.error("Error validating signature", e);
            return false;
        }
    }
    
    private boolean isRequestTimestampValid(String timestamp) {
        try {
            long requestTime = Long.parseLong(timestamp);
            long currentTime = Instant.now().getEpochSecond();
            long age = Math.abs(currentTime - requestTime);
            return age <= MAX_REQUEST_AGE_SECONDS;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    private String generateHmac(String data, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(
                    secret.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            );
            mac.init(secretKey);
            byte[] hmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hmac);
        } catch (Exception e) {
            log.error("Error generating HMAC", e);
            throw new RuntimeException("Failed to generate signature", e);
        }
    }
    
    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) {
            return a == b;
        }
        
        byte[] aBytes = a.getBytes(StandardCharsets.UTF_8);
        byte[] bBytes = b.getBytes(StandardCharsets.UTF_8);
        
        if (aBytes.length != bBytes.length) {
            return false;
        }
        
        int result = 0;
        for (int i = 0; i < aBytes.length; i++) {
            result |= aBytes[i] ^ bBytes[i];
        }
        
        return result == 0;
    }
    
    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }
    
    private void sendError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String jsonResponse = String.format(
                "{\"error\":\"%s\",\"status\":%d}",
                message,
                status
        );
        
        response.getWriter().write(jsonResponse);
    }
}
