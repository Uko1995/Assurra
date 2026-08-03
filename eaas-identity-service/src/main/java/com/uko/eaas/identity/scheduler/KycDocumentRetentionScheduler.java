package com.uko.eaas.identity.scheduler;

import com.uko.eaas.identity.model.entity.KycDocument;
import com.uko.eaas.identity.repository.KycDocumentRepository;
import com.uko.eaas.identity.service.CloudinaryStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduled job that enforces KYC document retention policies.
 * CBN requirement: transaction records 7 years; KYC docs typically 5 years post-account-closure.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KycDocumentRetentionScheduler {

    private final KycDocumentRepository kycDocumentRepository;
    private final CloudinaryStorageService cloudinaryStorageService;

    // Configurable retention period (default: 5 years)
    private static final int RETENTION_YEARS = 5;

    @Scheduled(cron = "0 0 2 * * ?") // Daily at 2 AM
    @Transactional
    public void enforceRetentionPolicy() {
        log.info("Running KYC document retention enforcement...");

        LocalDateTime cutoff = LocalDateTime.now().minusYears(RETENTION_YEARS);

        List<KycDocument> expiredDocs = kycDocumentRepository.findByUploadedAtBefore(cutoff);

        int deletedCount = 0;
        for (KycDocument doc : expiredDocs) {
            try {
                // Delete from Cloudinary
                if (doc.getFileUrl() != null) {
                    cloudinaryStorageService.deleteFile(doc.getFileUrl());
                }
                // Soft-delete in DB
                kycDocumentRepository.delete(doc);
                deletedCount++;
            } catch (Exception e) {
                log.error("Failed to delete expired KYC document {}: {}", doc.getId(), e.getMessage());
            }
        }

        log.info("KYC retention enforcement complete. Deleted {} expired documents.", deletedCount);
    }
}
