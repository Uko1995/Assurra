package com.uko.eaas.identity.model.entity;

import com.uko.eaas.identity.converter.EncryptedStringConverter;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "merchant_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MerchantProfile {

    @Id
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "business_name", nullable = false)
    private String businessName;

    @Column(name = "business_type", length = 100)
    private String businessType;

    @Column(name = "business_reg_number", length = 100)
    private String businessRegNumber;

    @Column(name = "bank_account_number", nullable = false, length = 255)
    @Convert(converter = EncryptedStringConverter.class)
    private String bankAccountNumber;

    @Column(name = "bank_code", nullable = false, length = 10)
    private String bankCode;

    @Column(name = "bank_name", nullable = false, length = 100)
    private String bankName;

    @Column(length = 255)
    @Convert(converter = EncryptedStringConverter.class)
    private String bvn;

    @Column(name = "settlement_email")
    private String settlementEmail;

    @Column(name = "api_key", length = 255)
    private String apiKey;

    @Column(name = "api_key_prefix", length = 20)
    private String apiKeyPrefix;

    @Column(name = "api_key_identifier", length = 128, unique = true)
    private String apiKeyIdentifier;

    @Column(name = "webhook_url", length = 500)
    private String webhookUrl;

    @Column(name = "webhook_secret", length = 255)
    private String webhookSecret;

    @Column(name = "is_verified")
    private Boolean isVerified = false;

    @Column(name = "kyc_submitted_at")
    private LocalDateTime kycSubmittedAt;

    @Column(name = "kyc_reviewed_at")
    private LocalDateTime kycReviewedAt;

    @Column(name = "kyc_reviewed_by")
    private UUID kycReviewedBy;

    @Column(name = "kyc_rejection_reason", columnDefinition = "TEXT")
    private String kycRejectionReason;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
