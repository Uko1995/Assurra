package com.uko.eaas.identity.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.uko.eaas.identity.messaging.event.UserAnonymizedEvent;
import com.uko.eaas.identity.model.entity.KycDocument;
import com.uko.eaas.identity.model.entity.User;
import com.uko.eaas.identity.repository.KycDocumentRepository;
import com.uko.eaas.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

/**
 * Service for GDPR/NDPR right to erasure.
 * Performs soft deletion with anonymization — hashed IDs allow audit trail retention
 * without retaining reversibly identifiable PII.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserAnonymizationService {

    private final UserRepository userRepository;
    private final KycDocumentRepository kycDocumentRepository;
    private final RabbitTemplate rabbitTemplate;
    private final Cloudinary cloudinary;

    @Transactional
    public void anonymizeUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        String anonymizedId = hashUuid(userId);

        user.setEmail("deleted-" + anonymizedId + "@anonymized.eaas");
        user.setPhone("0000000000");
        user.setFullName("Deleted User");
        user.setPasswordHash("ANONYMIZED");
        user.setActive(false);
        user.setEmailVerifyToken(null);
        user.setMarketingConsent(false);
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);

        publishUserAnonymizedEvent(userId, user.getRole().name());
        deleteKycDocuments(userId);

        log.info("User {} anonymized successfully", userId);
    }

    private String hashUuid(UUID uuid) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(uuid.toString().getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash).substring(0, 16);
        } catch (Exception e) {
            return uuid.toString().substring(0, 8);
        }
    }

    private void publishUserAnonymizedEvent(UUID userId, String role) {
        try {
            UserAnonymizedEvent event = UserAnonymizedEvent.builder()
                    .userId(userId)
                    .userRole(role)
                    .timestamp(Instant.now())
                    .build();
            rabbitTemplate.convertAndSend("eaas.exchange", "user.anonymized", event);
            log.info("Published user.anonymized event for userId={} role={}", userId, role);
        } catch (Exception e) {
            log.error("Failed to publish user.anonymized event for userId={}: {}", userId, e.getMessage(), e);
        }
    }

    private void deleteKycDocuments(UUID userId) {
        try {
            List<KycDocument> documents = kycDocumentRepository.findByMerchantId(userId);
            for (KycDocument doc : documents) {
                try {
                    String publicId = extractCloudinaryPublicId(doc.getFileUrl());
                    if (publicId != null) {
                        cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
                    }
                } catch (Exception e) {
                    log.error("Failed to delete KYC document {} from Cloudinary: {}", doc.getId(), e.getMessage());
                }
            }
            if (!documents.isEmpty()) {
                kycDocumentRepository.deleteAll(documents);
                log.info("Deleted {} KYC documents for anonymized user {}", documents.size(), userId);
            }
        } catch (Exception e) {
            log.error("Failed to delete KYC documents for userId={}: {}", userId, e.getMessage(), e);
        }
    }

    private String extractCloudinaryPublicId(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) return null;
        try {
            String[] parts = fileUrl.split("/upload/");
            if (parts.length < 2) return null;
            String afterUpload = parts[1];
            int versionSlash = afterUpload.indexOf('/');
            if (versionSlash < 0) return null;
            String publicIdWithExtension = afterUpload.substring(versionSlash + 1);
            int dotIndex = publicIdWithExtension.lastIndexOf('.');
            return dotIndex > 0 ? publicIdWithExtension.substring(0, dotIndex) : publicIdWithExtension;
        } catch (Exception e) {
            log.warn("Failed to extract Cloudinary public ID from URL: {}", fileUrl);
            return null;
        }
    }
}
