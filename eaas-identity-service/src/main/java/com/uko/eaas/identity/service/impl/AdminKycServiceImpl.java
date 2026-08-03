package com.uko.eaas.identity.service.impl;

import com.uko.eaas.identity.dto.ApiKeyResponse;
import com.uko.eaas.identity.dto.KycPendingMerchantResponse;
import com.uko.eaas.identity.dto.KycReviewRequest;
import com.uko.eaas.identity.dto.KycReviewResponse;
import com.uko.eaas.identity.dto.MerchantResponse;
import com.uko.eaas.identity.exception.ConflictException;
import com.uko.eaas.identity.exception.NotFoundException;
import com.uko.eaas.identity.model.entity.MerchantProfile;
import com.uko.eaas.identity.model.entity.User;
import com.uko.eaas.identity.model.enums.KycStatus;
import com.uko.eaas.identity.model.enums.UserRole;
import com.uko.eaas.identity.repository.MerchantProfileRepository;
import com.uko.eaas.identity.repository.UserRepository;
import com.uko.eaas.identity.service.AdminKycService;
import com.uko.eaas.identity.service.AuditService;
import com.uko.eaas.identity.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminKycServiceImpl implements AdminKycService {

    private final UserRepository userRepository;
    private final MerchantProfileRepository merchantProfileRepository;
    private final AuthServiceImpl authService;
    private final AuditService auditService;
    private final EmailService emailService;

    @Override
    public Page<KycPendingMerchantResponse> getPendingKycMerchants(Pageable pageable) {
        log.info("Fetching merchants pending KYC review");
        
        Page<User> pendingUsers = userRepository.findByRoleAndKycStatus(
                UserRole.MERCHANT, KycStatus.PENDING, pageable);
        
        return pendingUsers.map(this::mapToPendingMerchantResponse);
    }

    @Override
    public Page<KycPendingMerchantResponse> getUnderReviewMerchants(Pageable pageable) {
        log.info("Fetching merchants under KYC review");
        
        Page<User> underReviewUsers = userRepository.findByRoleAndKycStatus(
                UserRole.MERCHANT, KycStatus.UNDER_REVIEW, pageable);
        
        return underReviewUsers.map(this::mapToPendingMerchantResponse);
    }

    @Override
    public KycPendingMerchantResponse getMerchantForReview(UUID merchantId) {
        log.info("Fetching merchant details for review: {}", merchantId);
        
        User user = userRepository.findById(merchantId)
                .orElseThrow(() -> new NotFoundException("Merchant not found"));
        
        if (user.getRole() != UserRole.MERCHANT) {
            throw new ConflictException("User is not a merchant");
        }
        
        return mapToPendingMerchantResponse(user);
    }

    @Override
    @Transactional
    public KycReviewResponse approveKyc(KycReviewRequest request, UUID adminId) {
        log.info("Admin {} approving KYC for merchant: {}", adminId, request.getMerchantId());
        
        // Call the existing approval logic from AuthServiceImpl
        MerchantResponse response = authService.approveKyc(request.getMerchantId(), adminId);
        
        return KycReviewResponse.builder()
                .merchantId(response.getUserId())
                .businessName(response.getBusinessName())
                .kycStatus(KycStatus.VERIFIED)
                .message("KYC approved successfully. API key generated.")
                .apiKey(response.getApiKey())
                .apiKeyPrefix(response.getApiKeyPrefix())
                .reviewedAt(response.getVerifiedAt())
                .reviewedBy(adminId)
                .adminNotes(request.getAdminNotes())
                .build();
    }

    @Override
    @Transactional
    public KycReviewResponse rejectKyc(KycReviewRequest request, UUID adminId) {
        log.info("Admin {} rejecting KYC for merchant: {}", adminId, request.getMerchantId());
        
        if (request.getRejectionReason() == null || request.getRejectionReason().isBlank()) {
            throw new IllegalArgumentException("Rejection reason is required");
        }
        
        // Call the existing rejection logic from AuthServiceImpl
        authService.rejectKyc(request.getMerchantId(), request.getRejectionReason(), adminId);
        
        // Get updated merchant info
        MerchantProfile profile = merchantProfileRepository.findByUserId(request.getMerchantId())
                .orElseThrow(() -> new NotFoundException("Merchant profile not found"));
        
        return KycReviewResponse.builder()
                .merchantId(request.getMerchantId())
                .businessName(profile.getBusinessName())
                .kycStatus(KycStatus.REJECTED)
                .message("KYC rejected. Merchant has been notified.")
                .reviewedAt(profile.getKycReviewedAt())
                .reviewedBy(adminId)
                .rejectionReason(request.getRejectionReason())
                .adminNotes(request.getAdminNotes())
                .build();
    }

    @Override
    @Transactional
    public KycReviewResponse putUnderReview(UUID merchantId, UUID adminId, String notes) {
        log.info("Admin {} putting merchant {} KYC under review", adminId, merchantId);

        User user = userRepository.findById(merchantId)
                .orElseThrow(() -> new NotFoundException("Merchant not found"));
        
        if (user.getRole() != UserRole.MERCHANT) {
            throw new ConflictException("User is not a merchant");
        }
        
        if (user.getKycStatus() == KycStatus.VERIFIED) {
            throw new ConflictException("Cannot put verified merchant under review");
        }
        
        user.setKycStatus(KycStatus.UNDER_REVIEW);
        userRepository.save(user);
        
        MerchantProfile profile = merchantProfileRepository.findByUserId(merchantId)
                .orElseThrow(() -> new NotFoundException("Merchant profile not found"));
        
        auditService.logAction("KYC_UNDER_REVIEW", merchantId, adminId, UserRole.ADMIN,
                "Merchant KYC put under manual review: " + notes);
        
        return KycReviewResponse.builder()
                .merchantId(merchantId)
                .businessName(profile.getBusinessName())
                .kycStatus(KycStatus.UNDER_REVIEW)
                .message("Merchant KYC put under review. Manual verification required.")
                .reviewedBy(adminId)
                .adminNotes(notes)
                .build();
    }

    @Override
    @Transactional
    public ApiKeyResponse regenerateApiKey(UUID merchantId, UUID adminId, String reason) {
        log.info("Admin {} regenerating API key for merchant: {}", adminId, merchantId);

        MerchantProfile profile = merchantProfileRepository.findByUserId(merchantId)
                .orElseThrow(() -> new NotFoundException("Merchant profile not found"));

        if (Boolean.FALSE.equals(profile.getIsVerified())) {
            throw new ConflictException("Merchant KYC must be verified before regenerating API key");
        }

        if (reason != null && reason.length() > 500) {
            throw new IllegalArgumentException("Reason must be 500 characters or less");
        }

        ApiKeyResponse response = authService.regenerateApiKey(merchantId);

        auditService.logApiKeyGeneratedAsync(merchantId, response.getPrefix(), UserRole.ADMIN);

        emailService.sendApiKeyRegeneratedEmail(profile.getUser(), response.getPrefix());

        return ApiKeyResponse.builder()
                .apiKey(response.getApiKey())
                .prefix(response.getPrefix())
                .createdAt(response.getCreatedAt())
                .message("API key regenerated by admin")
                .warning(response.getWarning())
                .build();
    }

    private KycPendingMerchantResponse mapToPendingMerchantResponse(User user) {
        MerchantProfile profile = merchantProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new NotFoundException("Merchant profile not found for user: " + user.getId()));
        
        return KycPendingMerchantResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .businessName(profile.getBusinessName())
                .businessType(profile.getBusinessType())
                .businessRegNumber(profile.getBusinessRegNumber())
                .bankName(profile.getBankName())
                .bankCode(profile.getBankCode())
                .bankAccountNumber(profile.getBankAccountNumber())
                .bvn(profile.getBvn())
                .kycSubmittedAt(profile.getKycSubmittedAt())
                .userCreatedAt(user.getCreatedAt())
                .emailVerified(user.getEmailVerified())
                .build();
    }
}
