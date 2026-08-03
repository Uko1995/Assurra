package com.uko.eaas.identity.service;

import com.uko.eaas.identity.model.entity.MerchantProfile;
import com.uko.eaas.identity.model.entity.User;
import com.uko.eaas.identity.security.AesGcmEncryptionService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import static com.uko.eaas.identity.config.CacheConfig.CACHE_MERCHANT_PROFILES;
import static com.uko.eaas.identity.config.CacheConfig.CACHE_USERS;
import static com.uko.eaas.identity.config.CacheConfig.CACHE_USERS_BY_EMAIL;

/**
 * PII encryption repair that inspects RAW stored column values.
 * <p>
 * Raw values are read with native queries so the JPA attribute converters are bypassed.
 * The previous implementation loaded entities through the converter, which already decrypts
 * on load, so it always saw plaintext and re-encrypted it on every startup — double-encrypting
 * the stored data. This version handles all three states:
 * <ul>
 *   <li>plaintext &rarr; encrypted once with the current master key</li>
 *   <li>single-encrypted &rarr; left untouched</li>
 *   <li>double-encrypted &rarr; collapsed back to a single encryption</li>
 * </ul>
 * <p>
 * Disabled by default. Run once with {@code app.pii-backfill.enabled=true}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PiiEncryptionBackfillService {

    private static final Pattern BASE64_PATTERN = Pattern.compile("^[A-Za-z0-9+/=]+$");

    @Value("${app.pii-backfill.enabled:false}")
    private boolean enabled;

    private final AesGcmEncryptionService encryptionService;
    private final CacheManager cacheManager;

    @PersistenceContext
    private EntityManager entityManager;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void backfill() {
        if (!enabled) {
            log.info("PII encryption backfill disabled (set app.pii-backfill.enabled=true to run)");
            return;
        }
        log.info("Starting PII encryption backfill...");

        int userCount = 0;
        List<Object[]> users = entityManager
                .createNativeQuery("SELECT CAST(id AS nvarchar(36)) AS id, phone FROM users")
                .getResultList();
        for (Object[] row : users) {
            String id = row[0] == null ? null : row[0].toString();
            String rawPhone = row[1] == null ? null : row[1].toString();
            String plaintext = resolveToPlaintext(rawPhone);
            if (id == null || plaintext == null) {
                continue;
            }
            User user = entityManager.find(User.class, UUID.fromString(id));
            user.setPhone(plaintext);
            userCount++;
        }
        if (userCount > 0) {
            log.info("Repaired phone for {} users", userCount);
            evict(CACHE_USERS, CACHE_USERS_BY_EMAIL);
        }

        int profileCount = 0;
        List<Object[]> profiles = entityManager
                .createNativeQuery("SELECT CAST(id AS nvarchar(36)) AS id, bank_account_number, bvn FROM merchant_profiles")
                .getResultList();
        for (Object[] row : profiles) {
            String id = row[0] == null ? null : row[0].toString();
            String rawAccount = row[1] == null ? null : row[1].toString();
            String rawBvn = row[2] == null ? null : row[2].toString();
            if (id == null) {
                continue;
            }
            MerchantProfile profile = entityManager.find(MerchantProfile.class, UUID.fromString(id));
            boolean modified = false;
            String accountPlaintext = resolveToPlaintext(rawAccount);
            if (accountPlaintext != null) {
                profile.setBankAccountNumber(accountPlaintext);
                modified = true;
            }
            String bvnPlaintext = resolveToPlaintext(rawBvn);
            if (bvnPlaintext != null) {
                profile.setBvn(bvnPlaintext);
                modified = true;
            }
            if (modified) {
                profileCount++;
            }
        }
        if (profileCount > 0) {
            log.info("Repaired PII for {} merchant profiles", profileCount);
            evict(CACHE_MERCHANT_PROFILES);
        }

        log.info("PII encryption backfill complete.");
    }

    /**
     * Reduces a raw stored value to plaintext:
     * <ul>
     *   <li>plaintext &rarr; returned as-is (will be encrypted on flush)</li>
     *   <li>single-encrypted &rarr; {@code null} (already correct, leave untouched)</li>
     *   <li>double-encrypted &rarr; real plaintext (collapses the extra layer)</li>
     *   <li>unresolvable (key mismatch / corruption) &rarr; {@code null}, logged</li>
     * </ul>
     */
    private String resolveToPlaintext(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        if (!looksEncrypted(raw)) {
            return raw;
        }
        try {
            String once = encryptionService.decrypt(raw);
            if (looksEncrypted(once)) {
                return encryptionService.decrypt(once);
            }
            return null;
        } catch (RuntimeException e) {
            log.warn("Skipping unresolvable encrypted value ({} chars): {}", raw.length(), e.getMessage());
            return null;
        }
    }

    private void evict(String... cacheNames) {
        for (String cacheName : cacheNames) {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
            }
        }
    }

    private boolean looksEncrypted(String value) {
        // AES-GCM ciphertext is base64 and at least 40 chars (12-byte IV + 1-byte payload + 16-byte tag)
        return value != null && value.length() >= 40 && BASE64_PATTERN.matcher(value).matches();
    }
}
