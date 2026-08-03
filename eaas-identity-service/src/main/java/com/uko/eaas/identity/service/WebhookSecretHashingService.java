package com.uko.eaas.identity.service;

import com.uko.eaas.identity.model.entity.MerchantProfile;
import com.uko.eaas.identity.repository.MerchantProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * One-time backfill service that hashes existing plaintext webhook secrets on startup.
 * After running, all webhook secrets are BCrypt hashes.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebhookSecretHashingService {

    private final MerchantProfileRepository merchantProfileRepository;
    private final PasswordEncoder passwordEncoder;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void backfill() {
        log.info("Starting webhook secret hashing backfill...");

        List<MerchantProfile> profiles = merchantProfileRepository.findAll();
        int count = 0;
        for (MerchantProfile profile : profiles) {
            String secret = profile.getWebhookSecret();
            if (secret != null && !secret.isBlank() && !looksHashed(secret)) {
                profile.setWebhookSecret(passwordEncoder.encode(secret));
                count++;
            }
        }

        if (count > 0) {
            merchantProfileRepository.saveAll(profiles);
            log.info("Hashed webhook secrets for {} merchant profiles", count);
        } else {
            log.info("No plaintext webhook secrets found (already hashed or none set).");
        }
    }

    private boolean looksHashed(String value) {
        // BCrypt hashes start with $2a$, $2b$, or $2y$
        return value != null && value.startsWith("$2");
    }
}
