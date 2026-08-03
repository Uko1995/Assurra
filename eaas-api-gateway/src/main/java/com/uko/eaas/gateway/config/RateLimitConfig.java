package com.uko.eaas.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

/**
 * Rate limiting configuration for the API Gateway.
 * 
 * Rate limits:
 * - Authenticated users: 100 requests per minute (replenished at 10/second)
 * - Public endpoints (by IP): 20 requests per minute (replenished at 2/second)
 * - Service-to-service: 1000 requests per minute (replenished at 100/second)
 * 
 * Redis is used as the backing store for distributed rate limiting.
 */
@Configuration
public class RateLimitConfig {

    /**
     * Default rate limiter for authenticated users.
     * 100 requests per minute with burst capacity of 150.
     */
    @Bean
    @Primary
    public RedisRateLimiter redisRateLimiter() {
        // Default replenish rate: 10 tokens/second (600/minute)
        // Default burst capacity: 150 tokens
        return new RedisRateLimiter(10, 150);
    }
    
    /**
     * Strict rate limiter for public endpoints (login, register, webhooks).
     * Prevents brute force attacks and abuse.
     * 2 tokens/second (120/minute) with burst of 30.
     */
    @Bean
    public RedisRateLimiter strictRateLimiter() {
        return new RedisRateLimiter(2, 30);
    }
    
    /**
     * Internal rate limiter for service-to-service communication.
     * High limit for internal traffic.
     * 100 tokens/second (6000/minute) with burst of 500.
     */
    @Bean
    public RedisRateLimiter internalRateLimiter() {
        return new RedisRateLimiter(100, 500);
    }

    /**
     * Key resolver that uses:
     * 1. API Key for merchant requests
     * 2. JWT token for authenticated users
     * 3. IP address as fallback
     * 
     * This ensures each client is rate-limited independently.
     */
    @Bean
    @Primary
    public KeyResolver userKeyResolver() {
        return exchange -> {
            // Priority 1: API Key (merchant requests)
            String apiKey = exchange.getRequest().getHeaders().getFirst("X-API-Key");
            if (apiKey != null && !apiKey.isEmpty()) {
                return Mono.just("apikey:" + hashKey(apiKey));
            }
            
            // Priority 2: JWT token (customer/admin requests)
            String authorization = exchange.getRequest().getHeaders().getFirst("Authorization");
            if (authorization != null && authorization.startsWith("Bearer ")) {
                // Use token hash as key (don't store full token)
                String token = authorization.substring(7);
                return Mono.just("jwt:" + hashKey(token));
            }
            
            // Priority 3: Service token
            String serviceToken = exchange.getRequest().getHeaders().getFirst("X-Service-Token");
            if (serviceToken != null && !serviceToken.isEmpty()) {
                return Mono.just("service:" + hashKey(serviceToken));
            }
            
            // Fallback: IP address with path
            String clientIp = getClientIp(exchange);
            String path = exchange.getRequest().getPath().value();
            return Mono.just("ip:" + clientIp + ":" + path);
        };
    }

    /**
     * IP-based key resolver for public endpoints.
     * More restrictive rate limiting by IP.
     */
    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            String clientIp = getClientIp(exchange);
            String path = exchange.getRequest().getPath().value();
            return Mono.just("public:" + clientIp + ":" + path);
        };
    }
    
    /**
     * Internal service key resolver.
     * For service-to-service communication.
     */
    @Bean
    public KeyResolver serviceKeyResolver() {
        return exchange -> {
            String serviceToken = exchange.getRequest().getHeaders().getFirst("X-Service-Token");
            if (serviceToken != null && !serviceToken.isEmpty()) {
                return Mono.just("internal:" + hashKey(serviceToken));
            }
            return Mono.just("internal:anonymous");
        };
    }
    
    /**
     * Gets the client IP address, handling X-Forwarded-For and X-Real-IP headers.
     */
    private String getClientIp(org.springframework.web.server.ServerWebExchange exchange) {
        // Check X-Forwarded-For header (from load balancer/proxy)
        String forwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isEmpty()) {
            // Get the first IP in the chain (original client)
            return forwardedFor.split(",")[0].trim();
        }
        
        // Check X-Real-IP header
        String realIp = exchange.getRequest().getHeaders().getFirst("X-Real-IP");
        if (realIp != null && !realIp.isEmpty()) {
            return realIp;
        }
        
        // Fallback to direct connection address
        if (exchange.getRequest().getRemoteAddress() != null 
                && exchange.getRequest().getRemoteAddress().getAddress() != null) {
            return exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
        }
        
        return "unknown";
    }
    
    /**
     * Creates a hash of a key for use in Redis.
     * Uses SHA-256 for consistent hashing.
     */
    private String hashKey(String key) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(key.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return java.util.Base64.getEncoder().encodeToString(hash).substring(0, 16);
        } catch (Exception e) {
            // Fallback to truncated key
            return key.length() > 16 ? key.substring(0, 16) : key;
        }
    }
}
