package com.uko.eaas.identity.converter;

import com.uko.eaas.identity.security.AesGcmEncryptionService;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * JPA AttributeConverter that automatically encrypts String fields on persist
 * and decrypts on load. Apply with @Convert(converter = EncryptedStringConverter.class)
 * on entity fields that store PII.
 */
@Slf4j
@Component
@Converter(autoApply = false)
public class EncryptedStringConverter implements AttributeConverter<String, String> {

    private static final Pattern BASE64_PATTERN = Pattern.compile("^[A-Za-z0-9+/=]+$");

    private static AesGcmEncryptionService encryptionService;

    @Autowired
    public void setEncryptionService(AesGcmEncryptionService service) {
        EncryptedStringConverter.encryptionService = service;
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null || encryptionService == null) {
            return attribute;
        }
        return encryptionService.encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null || encryptionService == null) {
            return dbData;
        }
        try {
            return encryptionService.decrypt(dbData);
        } catch (RuntimeException e) {
            if (looksEncrypted(dbData)) {
                // Stored value looks like ciphertext but cannot be decrypted with the current
                // ENCRYPTION_MASTER_KEY. Fail loudly instead of leaking raw ciphertext into
                // the application (e.g. into payouts or API responses).
                log.error("Cannot decrypt stored ciphertext ({} chars) with the configured master key; "
                        + "refusing to pass through raw ciphertext", dbData.length());
                throw e;
            }
            // Legacy plaintext that predates PII encryption: return as-is.
            log.warn("Stored value is not encrypted (legacy plaintext); returning as-is");
            return dbData;
        }
    }

    private boolean looksEncrypted(String value) {
        // AES-GCM ciphertext is base64 and at least 40 chars (12-byte IV + 1-byte payload + 16-byte tag)
        return value != null && value.length() >= 40 && BASE64_PATTERN.matcher(value).matches();
    }
}
