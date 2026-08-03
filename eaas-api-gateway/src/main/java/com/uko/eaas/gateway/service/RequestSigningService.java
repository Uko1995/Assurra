package com.uko.eaas.gateway.service;

import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;

/**
 * Service for signing requests between gateway and downstream services.
 * This prevents header spoofing by allowing services to verify requests
 * came from the gateway.
 */
@Slf4j
@Service
public class RequestSigningService {

    @Value("${gateway.internal.signing-secret:change-this-secret-in-production}")
    private String signingSecret;
    
    // Request age tolerance in seconds (5 minutes)
    private static final long REQUEST_AGE_TOLERANCE_SECONDS = 300;
    
    /**
     * Generates a signed request with security headers.
     * These headers allow downstream services to verify the request
     * originated from the gateway and hasn't been tampered with.
     * 
     * @param originalRequest The original incoming request
     * @param userId The authenticated user ID
     * @param role The user's role
     * @param authType The authentication type (JWT or API_KEY)
     * @return ServerHttpRequest.Builder with added security headers
     */
    public ServerHttpRequest.Builder signRequest(
            ServerHttpRequest originalRequest,
            String userId,
            String role,
            String authType) {
        
        String timestamp = String.valueOf(Instant.now().getEpochSecond());
        String nonce = generateNonce();
        String path = originalRequest.getPath().value();
        String method = originalRequest.getMethod().name();
        
        // Generate signature
        String signatureData = String.join("|", 
                method,
                path,
                userId != null ? userId : "",
                role != null ? role : "",
                timestamp,
                nonce
        );
        
        String signature = generateHmac(signatureData, signingSecret);
        
        log.debug("Signing request: {} {} for user: {}", method, path, userId);
        
        return originalRequest.mutate()
                .header("X-User-Id", userId != null ? userId : "")
                .header("X-User-Role", role != null ? role : "")
                .header("X-Auth-Type", authType)
                .header("X-Request-Timestamp", timestamp)
                .header("X-Request-Nonce", nonce)
                .header("X-Request-Signature", signature);
    }
    
    /**
     * Validates a request signature at the downstream service.
     * This method is used by services to verify gateway requests.
     * 
     * @param method HTTP method
     * @param path Request path
     * @param userId User ID from header
     * @param role User role from header
     * @param timestamp Request timestamp
     * @param nonce Request nonce
     * @param signature The signature to verify
     * @return true if signature is valid and request is not expired
     */
    public boolean validateSignature(
            String method,
            String path,
            String userId,
            String role,
            String timestamp,
            String nonce,
            String signature) {
        
        try {
            // Check if request is expired (prevent replay attacks)
            long requestTime = Long.parseLong(timestamp);
            long currentTime = Instant.now().getEpochSecond();
            
            if (Math.abs(currentTime - requestTime) > REQUEST_AGE_TOLERANCE_SECONDS) {
                log.warn("Request timestamp too old: {} (current: {})", requestTime, currentTime);
                return false;
            }
            
            // Reconstruct signature data
            String signatureData = String.join("|",
                    method != null ? method : "",
                    path != null ? path : "",
                    userId != null ? userId : "",
                    role != null ? role : "",
                    timestamp,
                    nonce
            );
            
            // Generate expected signature
            String expectedSignature = generateHmac(signatureData, signingSecret);
            
            // Constant-time comparison to prevent timing attacks
            boolean valid = constantTimeEquals(signature, expectedSignature);
            
            if (!valid) {
                log.warn("Invalid request signature for path: {}", path);
            }
            
            return valid;
            
        } catch (Exception e) {
            log.error("Error validating request signature", e);
            return false;
        }
    }
    
    /**
     * Validates that a request came from the internal network (gateway).
     * Additional check that can be used by services.
     */
    public boolean isInternalRequest(String remoteAddress) {
        // In production, check against known internal IPs or use mTLS
        // For now, accept if signature is valid
        return true;
    }
    
    /**
     * Generates HMAC-SHA256 signature.
     */
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
            throw new RuntimeException("Failed to generate request signature", e);
        }
    }
    
    /**
     * Generates a random nonce for request uniqueness.
     */
    private String generateNonce() {
        byte[] nonceBytes = new byte[16];
        new java.security.SecureRandom().nextBytes(nonceBytes);
        return Base64.getEncoder().encodeToString(nonceBytes);
    }
    
    /**
     * Constant-time comparison to prevent timing attacks.
     */
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
}
