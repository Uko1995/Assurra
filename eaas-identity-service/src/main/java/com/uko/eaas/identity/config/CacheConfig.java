package com.uko.eaas.identity.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Redis Cache Configuration for Performance Optimization.
 *
 * Caches frequently accessed data to reduce database load and improve response times.
 */
@Slf4j
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Cache names used in the application
     */
    public static final String CACHE_USERS = "users";
    public static final String CACHE_USERS_BY_EMAIL = "usersByEmail";
    public static final String CACHE_MERCHANT_PROFILES = "merchantProfiles";
    public static final String CACHE_KYC_STATUS = "kycStatus";

    /**
     * Creates an ObjectMapper with Java 8 date/time support for Redis serialization.
     */
    private ObjectMapper createRedisObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // CRITICAL FIX: Enable default typing so Redis can deserialize objects back
        // to their concrete types (e.g., User) instead of LinkedHashMap.
        // Safe for internal cache since we control both serialization and deserialization.
        objectMapper.activateDefaultTyping(
                BasicPolymorphicTypeValidator.builder()
                        .allowIfBaseType(Object.class)
                        .build(),
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );
        return objectMapper;
    }

    /**
     * Creates a GenericJackson2JsonRedisSerializer with proper Java 8 date/time support.
     */
    private GenericJackson2JsonRedisSerializer createJsonSerializer() {
        return new GenericJackson2JsonRedisSerializer(createRedisObjectMapper());
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        // Default cache configuration
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(createJsonSerializer()))
                .disableCachingNullValues();

        // Specific cache configurations with different TTLs
        RedisCacheConfiguration userCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(15))  // User data cached for 15 minutes
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(createJsonSerializer()));

        RedisCacheConfiguration merchantCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))  // Merchant data cached for 10 minutes
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(createJsonSerializer()));

        RedisCacheConfiguration kycCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(5))  // KYC status changes frequently, cache for 5 minutes
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(createJsonSerializer()));

        RedisCacheManager cacheManager = RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(defaultConfig)
                .withCacheConfiguration(CACHE_USERS, userCacheConfig)
                .withCacheConfiguration(CACHE_USERS_BY_EMAIL, userCacheConfig)
                .withCacheConfiguration(CACHE_MERCHANT_PROFILES, merchantCacheConfig)
                .withCacheConfiguration(CACHE_KYC_STATUS, kycCacheConfig)
                .transactionAware()
                .build();

        log.info("Redis cache manager configured with caches: {}, {}, {}, {}",
                CACHE_USERS, CACHE_USERS_BY_EMAIL, CACHE_MERCHANT_PROFILES, CACHE_KYC_STATUS);

        return cacheManager;
    }
}
