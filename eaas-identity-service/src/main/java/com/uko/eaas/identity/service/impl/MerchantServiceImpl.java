package com.uko.eaas.identity.service.impl;

import com.uko.eaas.identity.dto.ApiKeyInfoResponse;
import com.uko.eaas.identity.dto.KycDocumentUploadResponse;
import com.uko.eaas.identity.dto.KycSubmitRequest;
import com.uko.eaas.identity.dto.KycSubmitResponse;
import com.uko.eaas.identity.dto.MerchantProfileResponse;
import com.uko.eaas.identity.dto.WebhookConfigRequest;
import com.uko.eaas.identity.dto.WebhookConfigResponse;
import com.uko.eaas.identity.util.PiiMaskingUtils;
import com.uko.eaas.identity.exception.ConflictException;
import com.uko.eaas.identity.exception.NotFoundException;
import com.uko.eaas.identity.model.entity.KycDocument;
import com.uko.eaas.identity.model.entity.KycSubmission;
import com.uko.eaas.identity.model.entity.MerchantProfile;
import com.uko.eaas.identity.model.entity.User;
import com.uko.eaas.identity.model.enums.DocumentType;
import com.uko.eaas.identity.model.enums.KycStatus;
import com.uko.eaas.identity.model.enums.UserRole;
import com.uko.eaas.identity.repository.KycDocumentRepository;
import com.uko.eaas.identity.repository.KycSubmissionRepository;
import com.uko.eaas.identity.repository.MerchantProfileRepository;
import com.uko.eaas.identity.repository.UserRepository;
import com.uko.eaas.identity.service.AuditService;
import com.uko.eaas.identity.service.CloudinaryStorageService;
import com.uko.eaas.identity.service.MerchantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantServiceImpl implements MerchantService {

    private final UserRepository userRepository;
    private final MerchantProfileRepository merchantProfileRepository;
    private final KycSubmissionRepository kycSubmissionRepository;
    private final KycDocumentRepository kycDocumentRepository;
    private final AuditService auditService;
    private final CloudinaryStorageService cloudinaryStorageService;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public KycSubmitResponse submitKyc(UUID merchantId, KycSubmitRequest request) {
        log.info("Merchant {} submitting KYC details", merchantId);

        User user = userRepository.findById(merchantId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (user.getRole() != UserRole.MERCHANT) {
            throw new ConflictException("Only merchants can submit KYC details");
        }

        MerchantProfile profile = merchantProfileRepository.findByUserId(merchantId)
                .orElseThrow(() -> new NotFoundException("Merchant profile not found"));

        // Update profile with submitted KYC details
        profile.setBusinessName(request.getBusinessName());
        profile.setBusinessType(request.getBusinessType());
        profile.setBusinessRegNumber(request.getBusinessRegNumber());
        profile.setBankAccountNumber(request.getBankAccountNumber());
        profile.setBankCode(request.getBankCode());
        profile.setBankName(request.getBankName());
        profile.setBvn(request.getBvn());

        // Reset Kyc state for resubmission
        profile.setKycSubmittedAt(LocalDateTime.now());
        profile.setKycReviewedAt(null);
        profile.setKycReviewedBy(null);
        profile.setKycRejectionReason(null);

        // Update user KYC status back to PENDING for review
        user.setKycStatus(KycStatus.PENDING);

        userRepository.save(user);
        merchantProfileRepository.save(profile);

        // Create or update KYC submission record
        KycSubmission submission = kycSubmissionRepository.findByMerchantId(merchantId)
                .map(existing -> {
                    existing.setStatus(KycStatus.PENDING);
                    existing.setSubmittedAt(LocalDateTime.now());
                    existing.setReviewedAt(null);
                    existing.setReviewedBy(null);
                    existing.setRejectionReason(null);
                    existing.setAdminNotes(null);
                    existing.setBusinessName(request.getBusinessName());
                    existing.setBankAccountNumber(request.getBankAccountNumber());
                    existing.setBankName(request.getBankName());
                    existing.setBvn(request.getBvn());
                    return existing;
                })
                .orElseGet(() -> KycSubmission.builder()
                        .merchantId(merchantId)
                        .status(KycStatus.PENDING)
                        .submittedAt(LocalDateTime.now())
                        .verificationMethod("MANUAL")
                        .businessName(request.getBusinessName())
                        .bankAccountNumber(request.getBankAccountNumber())
                        .bankName(request.getBankName())
                        .bvn(request.getBvn())
                        .build());

        kycSubmissionRepository.save(submission);

        auditService.logKycSubmittedAsync(merchantId, request.getBusinessName());

        log.info("KYC submitted successfully for merchant: {}", merchantId);

        return KycSubmitResponse.builder()
                .userId(merchantId)
                .businessName(request.getBusinessName())
                .kycStatus(KycStatus.PENDING)
                .kycSubmittedAt(profile.getKycSubmittedAt())
                .message("KYC details submitted successfully and are pending review.")
                .build();
    }

    @Override
    @Transactional
    public KycDocumentUploadResponse uploadKycDocument(UUID merchantId, MultipartFile file, DocumentType documentType) {
        log.info("Merchant {} uploading KYC document: {}", merchantId, documentType);

        User user = userRepository.findById(merchantId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (user.getRole() != UserRole.MERCHANT) {
            throw new ConflictException("Only merchants can upload KYC documents");
        }

        // Validate file
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("File size exceeds 10MB limit");
        }
        String contentType = file.getContentType();
        if (contentType == null ||
                (!contentType.startsWith("image/") && !contentType.equals("application/pdf"))) {
            throw new IllegalArgumentException("Only images and PDF files are allowed");
        }

        String folder = "kyc/" + merchantId;
        String fileUrl;
        try {
            fileUrl = cloudinaryStorageService.uploadFile(file.getOriginalFilename(), contentType, file.getBytes(), folder);
        } catch (Exception e) {
            log.error("Failed to upload KYC document for merchant {}: {}", merchantId, e.getMessage());
            throw new RuntimeException("Failed to upload KYC document", e);
        }

        KycDocument document = KycDocument.builder()
                .merchantId(merchantId)
                .documentType(documentType)
                .fileUrl(fileUrl)
                .fileName(file.getOriginalFilename())
                .fileSizeKb((int) (file.getSize() / 1024))
                .mimeType(contentType)
                .build();

        document = kycDocumentRepository.save(document);

        auditService.logActionAsync("KYC_DOCUMENT_UPLOADED", merchantId, merchantId, user.getRole(), null);

        log.info("KYC document uploaded successfully for merchant: {} documentId: {}", merchantId, document.getId());

        return KycDocumentUploadResponse.builder()
                .id(document.getId())
                .merchantId(document.getMerchantId())
                .documentType(document.getDocumentType())
                .fileUrl(document.getFileUrl())
                .fileName(document.getFileName())
                .fileSizeKb(document.getFileSizeKb())
                .mimeType(document.getMimeType())
                .uploadedAt(document.getUploadedAt())
                .build();
    }

    @Override
    @Transactional
    public WebhookConfigResponse configureWebhook(UUID merchantId, WebhookConfigRequest request) {
        log.info("Merchant {} configuring webhook: {}", merchantId, request.getUrl());

        User user = userRepository.findById(merchantId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (user.getRole() != UserRole.MERCHANT) {
            throw new ConflictException("Only merchants can configure webhooks");
        }

        MerchantProfile profile = merchantProfileRepository.findByUserId(merchantId)
                .orElseThrow(() -> new NotFoundException("Merchant profile not found"));

        profile.setWebhookUrl(request.getUrl());
        // Always regenerate webhook secret on configuration change for security
        String rawSecret = generateWebhookSecret();
        profile.setWebhookSecret(passwordEncoder.encode(rawSecret));
        merchantProfileRepository.save(profile);

        auditService.logActionAsync("WEBHOOK_CONFIGURED", merchantId, merchantId, UserRole.MERCHANT,
                "Webhook URL updated to: " + request.getUrl());

        log.info("Webhook configured for merchant: {}", merchantId);

        return WebhookConfigResponse.builder()
                .merchantId(merchantId)
                .webhookUrl(profile.getWebhookUrl())
                .events(request.getEvents())
                .message("Webhook configured successfully")
                .build();
    }

    private String generateWebhookSecret() {
        return "whsec_" + java.util.UUID.randomUUID().toString().replace("-", "");
    }

    @Override
    @Transactional(readOnly = true)
    public MerchantProfileResponse getProfile(UUID merchantId) {
        log.info("Fetching profile for merchant: {}", merchantId);

        User user = userRepository.findById(merchantId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (user.getRole() != UserRole.MERCHANT) {
            throw new ConflictException("Only merchants have a merchant profile");
        }

        MerchantProfile profile = merchantProfileRepository.findByUserId(merchantId)
                .orElseThrow(() -> new NotFoundException("Merchant profile not found"));

        return MerchantProfileResponse.builder()
                .userId(user.getId())
                .email(PiiMaskingUtils.maskEmail(user.getEmail()))
                .fullName(user.getFullName())
                .phone(PiiMaskingUtils.maskPhone(user.getPhone()))
                .kycStatus(user.getKycStatus())
                .emailVerified(user.getEmailVerified())
                .isActive(user.getIsActive())
                .lastLoginAt(user.getLastLoginAt())
                .businessName(profile.getBusinessName())
                .businessType(profile.getBusinessType())
                .businessRegNumber(profile.getBusinessRegNumber())
                .bankAccountNumber(PiiMaskingUtils.maskAccountNumber(profile.getBankAccountNumber()))
                .bankCode(profile.getBankCode())
                .bankName(profile.getBankName())
                .bvn(PiiMaskingUtils.maskBvn(profile.getBvn()))
                .settlementEmail(PiiMaskingUtils.maskEmail(profile.getSettlementEmail()))
                .apiKeyPrefix(profile.getApiKeyPrefix())
                .webhookUrl(profile.getWebhookUrl())
                .isVerified(profile.getIsVerified())
                .kycSubmittedAt(profile.getKycSubmittedAt())
                .kycReviewedAt(profile.getKycReviewedAt())
                .kycRejectionReason(profile.getKycRejectionReason())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ApiKeyInfoResponse getApiKeyInfo(UUID merchantId) {
        log.info("Fetching API key info for merchant: {}", merchantId);

        MerchantProfile profile = merchantProfileRepository.findByUserId(merchantId)
                .orElseThrow(() -> new NotFoundException("Merchant profile not found"));

        return ApiKeyInfoResponse.builder()
                .merchantId(merchantId)
                .businessName(profile.getBusinessName())
                .apiKeyPrefix(profile.getApiKeyPrefix())
                .apiKeyIdentifier(profile.getApiKeyIdentifier())
                .isVerified(profile.getIsVerified())
                .verifiedAt(profile.getKycReviewedAt())
                .build();
    }

    @Override
    @Transactional
    public String regenerateMerchantApiKey(UUID merchantId) {
        log.info("Merchant self-service API key regeneration: {}", merchantId);

        MerchantProfile profile = merchantProfileRepository.findByUserId(merchantId)
                .orElseThrow(() -> new NotFoundException("Merchant profile not found"));

        String newApiKey = generateApiKey();
        String apiKeyHash = passwordEncoder.encode(newApiKey);
        String apiKeyIdentifier = deriveApiKeyIdentifier(newApiKey);

        profile.setApiKey(apiKeyHash);
        profile.setApiKeyIdentifier(apiKeyIdentifier);
        merchantProfileRepository.save(profile);

        auditService.logApiKeyGeneratedAsync(merchantId, profile.getApiKeyPrefix(), UserRole.MERCHANT);

        log.info("API key regenerated by merchant: {}", merchantId);
        return newApiKey;
    }

    private String generateApiKey() {
        java.security.SecureRandom random = new java.security.SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return "sk_live_" + java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String deriveApiKeyIdentifier(String plainApiKey) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(plainApiKey.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
