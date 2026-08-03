package com.uko.eaas.gateway.filter;

import com.uko.eaas.gateway.service.ApiKeyValidationService;
import com.uko.eaas.gateway.service.RequestSigningService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Unified authentication filter that handles:
 * - JWT token validation for customers and admins
 * - API key validation for merchants
 * - Request signing for downstream service verification
 * 
 * This filter replaces the original JwtAuthFilter and provides
 * comprehensive authentication at the gateway level.
 */
@Slf4j
@Component
public class UnifiedAuthFilter extends AbstractGatewayFilterFactory<UnifiedAuthFilter.Config> {

    private final String jwtSecret;
    private final ApiKeyValidationService apiKeyValidationService;
    private final RequestSigningService requestSigningService;
    
    // Paths that don't require authentication
    private static final List<String> PUBLIC_PATHS = List.of(
        "/api/v1/auth/register",
        "/api/v1/auth/login",
        "/api/v1/auth/refresh",
        "/api/v1/auth/forgot-password",
        "/api/v1/auth/reset-password",
        "/api/v1/webhooks/",
        "/actuator/",
        "/health",
        "/api/v1/health"
    );
    
    // Cached signing key for JWT validation
    private SecretKey signingKey;

    public UnifiedAuthFilter(
            @Value("${jwt.secret}") String jwtSecret,
            ApiKeyValidationService apiKeyValidationService,
            RequestSigningService requestSigningService) {
        super(Config.class);
        this.jwtSecret = jwtSecret;
        this.apiKeyValidationService = apiKeyValidationService;
        this.requestSigningService = requestSigningService;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getPath().value();

            // Skip auth for public paths
            if (isPublicPath(path)) {
                log.debug("Public path accessed: {}", path);
                return chain.filter(exchange);
            }

            // Check for API Key (merchant requests)
            String apiKey = request.getHeaders().getFirst("X-API-Key");
            if (apiKey != null && !apiKey.isEmpty()) {
                return validateApiKeyAndProceed(apiKey, exchange, chain);
            }

            // Check for JWT (customer/admin requests)
            String authHeader = request.getHeaders().getFirst("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                return validateJwtAndProceed(authHeader, exchange, chain);
            }
            
            // Check for internal service token (service-to-service)
            String serviceToken = request.getHeaders().getFirst("X-Service-Token");
            if (serviceToken != null && !serviceToken.isEmpty()) {
                return validateServiceTokenAndProceed(serviceToken, exchange, chain);
            }

            // No valid authentication found
            log.warn("Unauthorized access attempt to: {} - No valid credentials provided", path);
            return onError(exchange, "Authentication required. Provide JWT token, API key, or service token.", 
                    HttpStatus.UNAUTHORIZED);
        };
    }
    
    /**
     * Validates API key and proceeds if valid.
     */
    private Mono<Void> validateApiKeyAndProceed(String apiKey, ServerWebExchange exchange, 
            org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
        
        return apiKeyValidationService.validateApiKey(apiKey)
                .flatMap(result -> {
                    if (!result.valid()) {
                        log.warn("Invalid API key provided for path: {}", 
                                exchange.getRequest().getPath().value());
                        return onError(exchange, "Invalid API key", HttpStatus.UNAUTHORIZED);
                    }
                    
                    log.debug("API key validated for merchant: {}", result.merchantId());
                    
                    // Sign request with merchant info
                    ServerHttpRequest signedRequest = requestSigningService
                            .signRequest(
                                    exchange.getRequest(),
                                    result.merchantId(),
                                    result.role(),
                                    "API_KEY"
                            )
                            .build();
                    
                    return chain.filter(exchange.mutate().request(signedRequest).build());
                })
                .onErrorResume(e -> {
                    log.error("Error validating API key", e);
                    return onError(exchange, "Authentication service error", 
                            HttpStatus.INTERNAL_SERVER_ERROR);
                });
    }
    
    /**
     * Validates JWT token and proceeds if valid.
     */
    private Mono<Void> validateJwtAndProceed(String authHeader, ServerWebExchange exchange,
            org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
        
        String token = authHeader.substring(7);
        
        try {
            Claims claims = validateToken(token);
            String userId = claims.getSubject();
            String role = claims.get("role", String.class);
            
            if (userId == null || role == null) {
                log.warn("JWT missing required claims (userId or role)");
                return onError(exchange, "Invalid token format", HttpStatus.UNAUTHORIZED);
            }
            
            log.debug("JWT validated for user: {} with role: {}", userId, role);
            
            // Sign request with user info
            ServerHttpRequest signedRequest = requestSigningService
                    .signRequest(
                            exchange.getRequest(),
                            userId,
                            role,
                            "JWT"
                    )
                    .build();
            
            return chain.filter(exchange.mutate().request(signedRequest).build());
            
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            log.warn("Expired JWT token");
            return onError(exchange, "Token expired", HttpStatus.UNAUTHORIZED);
        } catch (io.jsonwebtoken.JwtException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return onError(exchange, "Invalid token", HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            log.error("Error validating JWT token", e);
            return onError(exchange, "Authentication error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Validates internal service token for service-to-service communication.
     */
    private Mono<Void> validateServiceTokenAndProceed(String serviceToken, ServerWebExchange exchange,
            org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
        
        // In production, validate against a secure service token store
        // For now, we'll validate format and proceed
        // TODO: Implement proper service token validation with HMAC
        
        if (serviceToken.length() < 32) {
            log.warn("Invalid service token format");
            return onError(exchange, "Invalid service token", HttpStatus.UNAUTHORIZED);
        }
        
        log.debug("Service token validated");
        
        // For service tokens, we don't add user info, just mark as internal
        ServerHttpRequest signedRequest = requestSigningService
                .signRequest(
                        exchange.getRequest(),
                        "SYSTEM",
                        "SERVICE",
                        "SERVICE_TOKEN"
                )
                .build();
        
        return chain.filter(exchange.mutate().request(signedRequest).build());
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    private Claims validateToken(String token) {
        if (signingKey == null) {
            signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        }
        
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().add("Content-Type", "application/json");
        
        String errorBody = String.format(
                "{\"error\":\"%s\",\"status\":%d,\"path\":\"%s\"}",
                message,
                status.value(),
                exchange.getRequest().getPath().value()
        );
        
        byte[] bytes = errorBody.getBytes(StandardCharsets.UTF_8);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
    }

    public static class Config {
        // Configuration properties if needed
        private boolean requireAuthentication = true;
        
        public boolean isRequireAuthentication() {
            return requireAuthentication;
        }
        
        public void setRequireAuthentication(boolean requireAuthentication) {
            this.requireAuthentication = requireAuthentication;
        }
    }
}
