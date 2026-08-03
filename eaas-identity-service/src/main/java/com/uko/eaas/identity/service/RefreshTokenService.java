package com.uko.eaas.identity.service;

import com.uko.eaas.identity.model.entity.RefreshToken;
import com.uko.eaas.identity.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.UUID;

/**
 * Refresh token service with rotation and reuse detection.
 * Uses SHA-256 hashing for token storage.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-expiration:604800000}")
    private long refreshExpirationMs;

    @Transactional
    public String createRefreshToken(UUID userId, String ipAddress, String userAgent) {
        String rawToken = generateRawToken();
        String tokenHash = hashToken(rawToken);

        RefreshToken token = RefreshToken.builder()
                .tokenHash(tokenHash)
                .userId(userId)
                .expiresAt(LocalDateTime.now().plus(refreshExpirationMs, ChronoUnit.MILLIS))
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();

        refreshTokenRepository.save(token);
        return rawToken;
    }

    public record RotationResult(UUID userId, String newRawToken) {}

    @Transactional
    public RotationResult rotateRefreshToken(String rawToken, String ipAddress, String userAgent) {
        String tokenHash = hashToken(rawToken);
        RefreshToken existing = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (Boolean.TRUE.equals(existing.getRevoked())) {
            throw new RuntimeException("Refresh token has been revoked");
        }

        if (existing.getUsedAt() != null) {
            log.warn("Refresh token reuse detected for user {}. Revoking all tokens.", existing.getUserId());
            revokeAllUserTokens(existing.getUserId());
            throw new RuntimeException("Token reuse detected. All sessions revoked.");
        }

        if (existing.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Refresh token expired");
        }

        // Mark old token as used
        existing.setUsedAt(LocalDateTime.now());
        refreshTokenRepository.save(existing);

        // Create new token
        String newRawToken = generateRawToken();
        String newTokenHash = hashToken(newRawToken);

        RefreshToken newToken = RefreshToken.builder()
                .tokenHash(newTokenHash)
                .userId(existing.getUserId())
                .replacedBy(existing.getId())
                .expiresAt(LocalDateTime.now().plus(refreshExpirationMs, ChronoUnit.MILLIS))
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();

        refreshTokenRepository.save(newToken);
        return new RotationResult(existing.getUserId(), newRawToken);
    }

    @Transactional
    public void revokeAllUserTokens(UUID userId) {
        refreshTokenRepository.findAll().stream()
                .filter(t -> t.getUserId().equals(userId) && !Boolean.TRUE.equals(t.getRevoked()))
                .forEach(t -> t.setRevoked(true));
    }

    private String generateRawToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[64];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash token", e);
        }
    }
}
