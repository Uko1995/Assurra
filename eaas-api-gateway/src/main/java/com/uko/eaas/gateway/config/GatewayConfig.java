package com.uko.eaas.gateway.config;

import com.uko.eaas.gateway.filter.UnifiedAuthFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

/**
 * Gateway routing configuration with:
 * - Unified authentication (JWT, API Key, Service Token)
 * - Rate limiting per client
 * - Circuit breaker patterns
 * - Retry policies
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class GatewayConfig {

    private final UnifiedAuthFilter unifiedAuthFilter;
    private final RedisRateLimiter redisRateLimiter;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // ============================================================
                // ESCROW SERVICE - Admin Fee Configuration (Auth Required)
                // Must precede identity admin route since /api/v1/admin/** is
                // caught by identity-service-admin below.
                // ============================================================
                .route("escrow-service-admin", r -> r
                        .path("/api/v1/admin/fee-configurations/**")
                        .filters(f -> f
                                .filter(unifiedAuthFilter.apply(new UnifiedAuthFilter.Config()))
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(redisRateLimiter)
                                        .setKeyResolver(new RateLimitConfig().userKeyResolver())
                                        .setStatusCode(HttpStatus.TOO_MANY_REQUESTS))
                                .retry(retryConfig -> retryConfig.setRetries(2))
                                .circuitBreaker(config -> config
                                        .setName("escrowAdminCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/escrow"))
                                .stripPrefix(0))
                        .uri("http://localhost:8082"))

                // ============================================================
                // IDENTITY SERVICE - Admin and Merchant Management (Auth Required)
                // ============================================================
                .route("identity-service-admin", r -> r
                        .path("/api/v1/merchants/**", "/api/v1/admin/kyc/**", "/api/v1/admin/**")
                        .filters(f -> f
                                .filter(unifiedAuthFilter.apply(new UnifiedAuthFilter.Config()))
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(redisRateLimiter)
                                        .setKeyResolver(new RateLimitConfig().userKeyResolver())
                                        .setStatusCode(HttpStatus.TOO_MANY_REQUESTS))
                                .retry(retryConfig -> retryConfig.setRetries(2))
                                .circuitBreaker(config -> config
                                        .setName("identityCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/identity"))
                                .stripPrefix(0))
                        .uri("http://localhost:8081"))

                // Identity Service - Public (No Auth Required)
                .route("identity-service-public", r -> r
                        .path("/api/v1/auth/**", "/api/v1/health")
                        .filters(f -> f
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(redisRateLimiter)
                                        .setKeyResolver(new RateLimitConfig().ipKeyResolver())
                                        .setStatusCode(HttpStatus.TOO_MANY_REQUESTS))
                                .retry(retryConfig -> retryConfig.setRetries(1))
                                .stripPrefix(0))
                        .uri("http://localhost:8081"))

                // ============================================================
                // ESCROW SERVICE - Core Business Logic (Auth Required)
                // ============================================================
                .route("escrow-service", r -> r
                        .path("/api/v1/escrow/**")
                        .filters(f -> f
                                .filter(unifiedAuthFilter.apply(new UnifiedAuthFilter.Config()))
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(redisRateLimiter)
                                        .setKeyResolver(new RateLimitConfig().userKeyResolver())
                                        .setStatusCode(HttpStatus.TOO_MANY_REQUESTS))
                                .retry(retryConfig -> retryConfig.setRetries(3))
                                .circuitBreaker(config -> config
                                        .setName("escrowCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/escrow"))
                                .stripPrefix(0))
                        .uri("http://localhost:8082"))

                // ============================================================
                // PAYMENT SERVICE - Payment Operations (Auth Required)
                // ============================================================
                .route("payment-service", r -> r
                        .path("/api/v1/payments/**", "/api/v1/payouts/**")
                        .filters(f -> f
                                .filter(unifiedAuthFilter.apply(new UnifiedAuthFilter.Config()))
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(redisRateLimiter)
                                        .setKeyResolver(new RateLimitConfig().userKeyResolver())
                                        .setStatusCode(HttpStatus.TOO_MANY_REQUESTS))
                                .retry(retryConfig -> retryConfig.setRetries(2))
                                .circuitBreaker(config -> config
                                        .setName("paymentCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/payment"))
                                .stripPrefix(0))
                        .uri("http://localhost:8083"))

                // Payment Webhooks - Public (PSP callbacks - No Auth, but HMAC verified downstream)
                .route("payment-webhooks", r -> r
                        .path("/api/v1/webhooks/**")
                        .filters(f -> f
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(redisRateLimiter)
                                        .setKeyResolver(new RateLimitConfig().ipKeyResolver())
                                        .setStatusCode(HttpStatus.TOO_MANY_REQUESTS))
                                .retry(retryConfig -> retryConfig.setRetries(1))
                                .stripPrefix(0))
                        .uri("http://localhost:8083"))

                // ============================================================
                // COMMUNICATION SERVICE - Notifications & Disputes (Auth Required)
                // ============================================================
                .route("communication-service", r -> r
                        .path("/api/v1/notifications/**", "/api/v1/disputes/**", "/api/v1/admin/disputes/**")
                        .filters(f -> f
                                .filter(unifiedAuthFilter.apply(new UnifiedAuthFilter.Config()))
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(redisRateLimiter)
                                        .setKeyResolver(new RateLimitConfig().userKeyResolver())
                                        .setStatusCode(HttpStatus.TOO_MANY_REQUESTS))
                                .retry(retryConfig -> retryConfig.setRetries(2))
                                .circuitBreaker(config -> config
                                        .setName("communicationCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/communication"))
                                .stripPrefix(0))
                        .uri("http://localhost:8084"))

                // ============================================================
                // FALLBACK ROUTES
                // ============================================================
                .route("fallback-identity", r -> r
                        .path("/fallback/identity")
                        .uri("forward:/fallback"))

                .route("fallback-escrow", r -> r
                        .path("/fallback/escrow")
                        .uri("forward:/fallback"))

                .route("fallback-payment", r -> r
                        .path("/fallback/payment")
                        .uri("forward:/fallback"))

                .route("fallback-communication", r -> r
                        .path("/fallback/communication")
                        .uri("forward:/fallback"))

                .build();
    }

    /**
     * Default fallback handler for circuit breaker
     */
    @Bean
    public org.springframework.web.reactive.function.server.RouterFunction<?> fallbackRoutes() {
        return org.springframework.web.reactive.function.server.RouterFunctions.route(
                org.springframework.web.reactive.function.server.RequestPredicates.path("/fallback/**"),
                request -> {
                    log.warn("Circuit breaker fallback triggered for: {}", request.path());
                    return org.springframework.web.reactive.function.server.ServerResponse
                            .status(HttpStatus.SERVICE_UNAVAILABLE)
                            .bodyValue(new FallbackResponse(
                                    "Service temporarily unavailable",
                                    HttpStatus.SERVICE_UNAVAILABLE.value(),
                                    request.path()
                            ));
                }
        );
    }

    public record FallbackResponse(String message, int status, String path) {}
}
