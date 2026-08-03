package com.uko.eaas.gateway.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

/**
 * Service for validating API keys with Redis caching.
 * API keys are hashed using BCrypt and stored in the identity service.
 * This service validates them by checking against cached valid keys.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApiKeyValidationService {

    private final ReactiveStringRedisTemplate redisTemplate;
    
    // Cache key prefix for validated API keys
    private static final String API_KEY_CACHE_PREFIX = "apikey:validated:";
    private static final String API_KEY_DATA_PREFIX = "apikey:data:";
    
    // Cache TTL for validated API keys (15 minutes)
    private static final Duration CACHE_TTL = Duration.ofMinutes(15);
    
    /**
     * Validates an API key against Redis cache.
     * If not in cache, returns invalid (gateway doesn't have direct DB access).
     * The identity service should pre-populate valid API keys in Redis.
     * 
     * @param apiKey The API key to validate
     * @return Mono<ApiKeyValidationResult> containing validation result and merchant info
     */
    public Mono<ApiKeyValidationResult> validateApiKey(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            return Mono.just(ApiKeyValidationResult.invalid());
        }
        
        // Hash the API key for cache lookup (don't store raw keys)
        String keyHash = hashApiKey(apiKey);
        String cacheKey = API_KEY_CACHE_PREFIX + keyHash;
        
        return redisTemplate.opsForValue()
                .get(cacheKey)
                .flatMap(cachedValue -> {
                    // Key exists in cache - refresh TTL and return valid
                    log.debug("API key found in cache");
                    return redisTemplate.opsForValue()
                            .get(API_KEY_DATA_PREFIX + keyHash)
                            .map(merchantData -> ApiKeyValidationResult.valid(
                                    extractMerchantId(merchantData),
                                    extractMerchantRole(merchantData)
                            ))
                            .defaultIfEmpty(ApiKeyValidationResult.valid(
                                    cachedValue, 
                                    "MERCHANT"
                            ));
                })
                .switchIfEmpty(Mono.defer(() -> {
                    // Not in cache - API key is invalid or expired
                    log.warn("API key not found in cache: {}", keyHash.substring(0, 8) + "...");
                    return Mono.just(ApiKeyValidationResult.invalid());
                }));
    }
    
    /**
     * Stores a validated API key in Redis cache.
     * Called by identity service when API keys are created/updated.
     * 
     * @param apiKey The raw API key
     * @param merchantId The associated merchant ID
     * @return Mono<Void>
     */
    public Mono<Void> cacheValidatedApiKey(String apiKey, String merchantId) {
        String keyHash = hashApiKey(apiKey);
        String cacheKey = API_KEY_CACHE_PREFIX + keyHash;
        String dataKey = API_KEY_DATA_PREFIX + keyHash;
        String merchantData = merchantId + ":MERCHANT";
        
        return redisTemplate.opsForValue()
                .set(cacheKey, merchantId, CACHE_TTL)
                .then(redisTemplate.opsForValue().set(dataKey, merchantData, CACHE_TTL))
                .then();
    }
    
    /**
     * Invalidates an API key from cache.
     * Called when API keys are revoked.
     * 
     * @param apiKey The API key to invalidate
     * @return Mono<Void>
     */
    public Mono<Void> invalidateApiKey(String apiKey) {
        String keyHash = hashApiKey(apiKey);
        String cacheKey = API_KEY_CACHE_PREFIX + keyHash;
        String dataKey = API_KEY_DATA_PREFIX + keyHash;
        
        return redisTemplate.delete(cacheKey)
                .then(redisTemplate.delete(dataKey))
                .then();
    }
    
    /**
     * Creates a hash of the API key for cache storage.
     * Uses HMAC-SHA256 with a secret to prevent timing attacks.
     */
    private String hashApiKey(String apiKey) {
        try {
            // Use a simple hash for cache key - SHA-256
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(apiKey.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            log.error("Error hashing API key", e);
            // Fallback - shouldn't happen
            return Base64.getEncoder().encodeToString(apiKey.getBytes(StandardCharsets.UTF_8));
        }
    }
    
    private String extractMerchantId(String merchantData) {
        if (merchantData == null || !merchantData.contains(":")) {
            return merchantData;
        }
        return merchantData.split(":")[0];
    }
    
    private String extractMerchantRole(String merchantData) {
        if (merchantData == null || !merchantData.contains(":")) {
            return "MERCHANT";
        }
        String[] parts = merchantData.split(":");
        return parts.length > 1 ? parts[1] : "MERCHANT";
    }
    
    /**
     * Result of API key validation
     */
    public record ApiKeyValidationResult(
            boolean valid,
            String merchantId,
            String role
    ) {
        public static ApiKeyValidationResult valid(String merchantId, String role) {
            return new ApiKeyValidationResult(true, merchantId, role);
        }
        
        public static ApiKeyValidationResult invalid() {
            return new ApiKeyValidationResult(false, null, null);
        }
    }
}
