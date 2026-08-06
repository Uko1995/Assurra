package com.uko.eaas.communication.service.impl;

import com.uko.eaas.communication.dto.*;
import com.uko.eaas.communication.model.entity.Dispute;
import com.uko.eaas.communication.model.entity.DisputeEvidence;
import com.uko.eaas.communication.model.entity.DisputeMessage;
import com.uko.eaas.communication.model.enums.DisputeStatus;
import com.uko.eaas.communication.repository.DisputeEvidenceRepository;
import com.uko.eaas.communication.repository.DisputeMessageRepository;
import com.uko.eaas.communication.repository.DisputeRepository;
import com.uko.eaas.communication.service.DisputeService;
import com.uko.eaas.communication.service.NotificationService;
import com.uko.eaas.communication.service.StorageService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class DisputeServiceImpl implements DisputeService {

    private final DisputeRepository disputeRepository;
    private final DisputeEvidenceRepository evidenceRepository;
    private final DisputeMessageRepository messageRepository;
    private final StorageService storageService;
    private final NotificationService notificationService;
    private final com.uko.eaas.communication.service.AuditPublisher auditPublisher;
    private final com.uko.eaas.communication.client.EscrowServiceClient escrowServiceClient;

    @Value("${notification.evidence.expiry-days:90}")
    private int evidenceExpiryDays;

    @Value("${cloudinary.cloud-name}")
    private String cloudName;

    @Override
    public DisputeResponse createDispute(CreateDisputeRequest request) {
        log.info("Creating dispute for escrow: {} by user: {}", request.getEscrowReference(), request.getRaisedBy());

        // Check if dispute already exists
        if (disputeRepository.existsByEscrowReference(request.getEscrowReference())) {
            throw new IllegalStateException("Dispute already exists for escrow: " + request.getEscrowReference());
        }

        // Validate the escrow is eligible and mark it as disputed (eligibility gate)
        escrowServiceClient.markDispute(request.getEscrowReference());

        String reference = generateReference();

        Dispute dispute = Dispute.builder()
                .reference(reference)
                .escrowReference(request.getEscrowReference())
                .customerId(UUID.fromString(request.getCustomerId()))
                .merchantId(UUID.fromString(request.getMerchantId()))
                .raisedBy(UUID.fromString(request.getRaisedBy()))
                .reason(request.getReason())
                .description(request.getDescription())
                .desiredOutcome(request.getDesiredOutcome())
                .status(DisputeStatus.OPEN)
                .amountDisputed(request.getAmountDisputed())
                .openedAt(LocalDateTime.now())
                .lastActivityAt(LocalDateTime.now())
                .customerNotified(false)
                .merchantNotified(false)
                .build();

        dispute = disputeRepository.save(dispute);

        // Notify parties
        notificationService.sendNotificationForEvent("dispute.opened", request.getCustomerId(), reference, null);
        notificationService.sendNotificationForEvent("dispute.opened", request.getMerchantId(), reference, null);

        auditPublisher.publish(com.uko.eaas.communication.messaging.event.AuditEvent.builder()
                .eventType("DISPUTE_CREATED")
                .entityType("DISPUTE")
                .entityId(reference)
                .action("CREATE")
                .performedBy(UUID.fromString(request.getRaisedBy()))
                .performedByRole("CUSTOMER")
                .metadata("{\"escrowReference\": \"" + request.getEscrowReference() + "\", \"reason\": \"" + request.getReason() + "\"}")
                .build());

        log.info("Dispute created with reference: {}", reference);
        return mapToResponse(dispute);
    }

    @Override
    @Transactional(readOnly = true)
    public DisputeResponse getDispute(String reference) {
        Dispute dispute = disputeRepository.findByReference(reference)
                .orElseThrow(() -> new EntityNotFoundException("Dispute not found: " + reference));
        return mapToResponse(dispute);
    }

    @Override
    @Transactional(readOnly = true)
    public DisputeResponse getDisputeByEscrow(String escrowReference) {
        return disputeRepository.findByEscrowReference(escrowReference)
                .map(this::mapToResponse)
                .orElseThrow(() -> new EntityNotFoundException("Dispute not found for escrow: " + escrowReference));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DisputeResponse> listDisputes(UUID userId, Pageable pageable) {
        return disputeRepository.findByUserId(userId, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DisputeResponse> listActiveDisputes(Pageable pageable) {
        return disputeRepository.findActiveDisputes(pageable)
                .map(this::mapToResponse);
    }

    @Override
    public DisputeResponse resolveDispute(String reference, ResolveDisputeRequest request, UUID resolvedBy) {
        log.info("Resolving dispute: {} with status: {}", reference, request.getResolution());

        Dispute dispute = disputeRepository.findByReference(reference)
                .orElseThrow(() -> new EntityNotFoundException("Dispute not found: " + reference));

        if (dispute.getStatus() == DisputeStatus.CLOSED) {
            throw new IllegalStateException("Dispute is already closed");
        }

        dispute.setStatus(request.getResolution());
        dispute.setResolutionAmount(request.getResolutionAmount());
        dispute.setResolutionNotes(request.getResolutionNotes());
        dispute.setResolvedBy(resolvedBy);
        dispute.setResolvedAt(LocalDateTime.now());
        dispute.setClosedAt(LocalDateTime.now());
        dispute.setLastActivityAt(LocalDateTime.now());

        dispute = disputeRepository.save(dispute);

        // Notify parties
        notificationService.sendNotificationForEvent("dispute.resolved", dispute.getCustomerId().toString(), reference, null);
        notificationService.sendNotificationForEvent("dispute.resolved", dispute.getMerchantId().toString(), reference, null);

        auditPublisher.publish(com.uko.eaas.communication.messaging.event.AuditEvent.builder()
                .eventType("DISPUTE_RESOLVED")
                .entityType("DISPUTE")
                .entityId(reference)
                .action("RESOLVE")
                .performedBy(resolvedBy)
                .performedByRole("ADMIN")
                .metadata("{\"resolution\": \"" + request.getResolution() + "\", \"resolutionAmount\": " + request.getResolutionAmount() + "}")
                .build());

        log.info("Dispute {} resolved", reference);

        finalizeEscrowIfNeeded(dispute, request.getResolution());

        return mapToResponse(dispute);
    }

    @Override
    public DisputeResponse updateDisputeStatus(String reference, DisputeStatus status) {
        Dispute dispute = disputeRepository.findByReference(reference)
                .orElseThrow(() -> new EntityNotFoundException("Dispute not found: " + reference));

        dispute.setStatus(status);
        dispute.setLastActivityAt(LocalDateTime.now());

        dispute = disputeRepository.save(dispute);

        // If the status is a terminal resolution, finalize the escrow in the escrow service
        finalizeEscrowIfNeeded(dispute, status);

        return mapToResponse(dispute);
    }

    @Override
    public DisputeMessageResponse addMessage(DisputeMessageRequest request) {
        log.info("Adding message to dispute: {} by {}", request.getDisputeId(), request.getSenderId());

        // Verify dispute exists
        if (!disputeRepository.existsById(request.getDisputeId())) {
            throw new EntityNotFoundException("Dispute not found: " + request.getDisputeId());
        }

        DisputeMessage message = DisputeMessage.builder()
                .disputeId(request.getDisputeId())
                .senderId(request.getSenderId())
                .senderType(request.getSenderType())
                .message(request.getMessage())
                .isInternal(request.getIsInternal())
                .hasAttachments(request.getAttachmentIds() != null && !request.getAttachmentIds().isEmpty())
                .attachmentIds(request.getAttachmentIds())
                .readByCustomer(false)
                .readByMerchant(false)
                .build();

        message = messageRepository.save(message);

        // Update dispute last activity
        Dispute dispute = disputeRepository.findById(request.getDisputeId()).orElseThrow();
        dispute.setLastActivityAt(LocalDateTime.now());
        disputeRepository.save(dispute);

        return mapToMessageResponse(message);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DisputeMessageResponse> getMessages(UUID disputeId, Pageable pageable) {
        return messageRepository.findByDisputeIdAndIsInternalFalseOrderByCreatedAtDesc(disputeId, pageable)
                .map(this::mapToMessageResponse);
    }

    @Override
    public void markMessagesAsRead(UUID disputeId, String readerType) {
        List<DisputeMessage> unreadMessages;

        if ("CUSTOMER".equals(readerType)) {
            unreadMessages = messageRepository.findByDisputeIdAndIsInternalFalseAndReadByCustomerFalse(disputeId);
            for (DisputeMessage message : unreadMessages) {
                message.setReadByCustomer(true);
            }
        } else if ("MERCHANT".equals(readerType)) {
            unreadMessages = messageRepository.findByDisputeIdAndIsInternalFalseAndReadByMerchantFalse(disputeId);
            for (DisputeMessage message : unreadMessages) {
                message.setReadByMerchant(true);
            }
        } else {
            return;
        }

        if (!unreadMessages.isEmpty()) {
            messageRepository.saveAll(unreadMessages);
            log.info("Marked {} messages as read for {} in dispute {}", unreadMessages.size(), readerType, disputeId);
        }
    }

    @Override
    public EvidenceResponse uploadEvidence(UUID disputeId, UUID uploadedBy, MultipartFile file, String description, String evidenceType) {
        try {
            return uploadEvidence(disputeId, uploadedBy, file.getOriginalFilename(), file.getContentType(),
                    file.getBytes(), description, evidenceType);
        } catch (Exception e) {
            log.error("Failed to upload evidence: {}", e.getMessage());
            throw new RuntimeException("Evidence upload failed", e);
        }
    }

    @Override
    public EvidenceResponse uploadEvidence(UUID disputeId, UUID uploadedBy, String fileName, String contentType,
                                           byte[] content, String description, String evidenceType) {
        log.info("Uploading evidence for dispute: {} by user: {}", disputeId, uploadedBy);

        // Verify dispute exists
        if (!disputeRepository.existsById(disputeId)) {
            throw new EntityNotFoundException("Dispute not found: " + disputeId);
        }

        // Generate unique filename
        String uniqueFileName = generateUniqueFileName(fileName);

        // Upload to Cloudinary
        String path = String.format("disputes/%s", disputeId);
        String cloudinaryUrl = storageService.uploadFile(uniqueFileName, contentType, content, path);

        // Calculate checksum
        String checksum = calculateChecksum(content);

        // Calculate expiry
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(evidenceExpiryDays);

        // Save evidence record
        DisputeEvidence evidence = DisputeEvidence.builder()
                .disputeId(disputeId)
                .uploadedBy(uploadedBy)
                .fileName(uniqueFileName)
                .originalName(fileName)
                .fileType(getFileExtension(fileName))
                .fileSizeBytes((long) content.length)
                .mimeType(contentType)
                .cloudinaryPublicId(cloudinaryUrl)
                .cloudinaryUrl(cloudinaryUrl)
                .description(description)
                .evidenceType(evidenceType)
                .checksum(checksum)
                .encrypted(false)
                .expiresAt(expiresAt)
                .build();

        evidence = evidenceRepository.save(evidence);

        // Update dispute last activity
        Dispute dispute = disputeRepository.findById(disputeId).orElseThrow();
        dispute.setLastActivityAt(LocalDateTime.now());
        disputeRepository.save(dispute);

        log.info("Evidence uploaded: {} for dispute {}", evidence.getId(), disputeId);
        return mapToEvidenceResponse(evidence);
    }

    @Override
    public void deleteEvidence(UUID evidenceId, UUID deletedBy) {
        DisputeEvidence evidence = evidenceRepository.findById(evidenceId)
                .orElseThrow(() -> new EntityNotFoundException("Evidence not found: " + evidenceId));

        // Soft delete
        evidence.setDeletedAt(LocalDateTime.now());
        evidence.setDeletedBy(deletedBy);
        evidenceRepository.save(evidence);

        // Also delete from Cloudinary
        try {
            storageService.deleteFile(evidence.getCloudinaryPublicId());
        } catch (Exception e) {
            log.warn("Failed to delete file from Cloudinary: {}", e.getMessage());
        }

        log.info("Evidence {} deleted by {}", evidenceId, deletedBy);
    }

    private void finalizeEscrowIfNeeded(Dispute dispute, DisputeStatus resolution) {
        if (resolution == DisputeStatus.RESOLVED_MERCHANT || resolution == DisputeStatus.RESOLVED_CUSTOMER) {
            escrowServiceClient.resolveDispute(dispute.getEscrowReference(), resolution.name());
        }
    }

    private String generateReference() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String random = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "DISP-" + timestamp + "-" + random;
    }

    private String generateUniqueFileName(String originalName) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String hash = UUID.randomUUID().toString().substring(0, 8);
        String extension = getFileExtension(originalName);
        return hash + "_" + timestamp + "." + extension;
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "bin";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }

    private String calculateChecksum(byte[] content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content);
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            return null;
        }
    }

    private DisputeResponse mapToResponse(Dispute dispute) {
        return DisputeResponse.builder()
                .id(dispute.getId())
                .reference(dispute.getReference())
                .escrowReference(dispute.getEscrowReference())
                .customerId(dispute.getCustomerId())
                .merchantId(dispute.getMerchantId())
                .raisedBy(dispute.getRaisedBy())
                .reason(dispute.getReason())
                .description(dispute.getDescription())
                .desiredOutcome(dispute.getDesiredOutcome())
                .status(dispute.getStatus())
                .amountDisputed(dispute.getAmountDisputed())
                .resolutionAmount(dispute.getResolutionAmount())
                .resolutionNotes(dispute.getResolutionNotes())
                .resolvedBy(dispute.getResolvedBy())
                .resolvedAt(dispute.getResolvedAt())
                .openedAt(dispute.getOpenedAt())
                .closedAt(dispute.getClosedAt())
                .lastActivityAt(dispute.getLastActivityAt())
                .customerNotified(dispute.getCustomerNotified())
                .merchantNotified(dispute.getMerchantNotified())
                .createdAt(dispute.getCreatedAt())
                .build();
    }

    private DisputeMessageResponse mapToMessageResponse(DisputeMessage message) {
        return DisputeMessageResponse.builder()
                .id(message.getId())
                .disputeId(message.getDisputeId())
                .senderId(message.getSenderId())
                .senderType(message.getSenderType())
                .message(message.getMessage())
                .isInternal(message.getIsInternal())
                .hasAttachments(message.getHasAttachments())
                .attachmentIds(message.getAttachmentIds())
                .readByCustomer(message.getReadByCustomer())
                .readByMerchant(message.getReadByMerchant())
                .readAt(message.getReadAt())
                .createdAt(message.getCreatedAt())
                .build();
    }

    private EvidenceResponse mapToEvidenceResponse(DisputeEvidence evidence) {
        return EvidenceResponse.builder()
                .id(evidence.getId())
                .disputeId(evidence.getDisputeId())
                .uploadedBy(evidence.getUploadedBy())
                .fileName(evidence.getFileName())
                .originalName(evidence.getOriginalName())
                .fileType(evidence.getFileType())
                .fileSizeBytes(evidence.getFileSizeBytes())
                .mimeType(evidence.getMimeType())
                .url(evidence.getCloudinaryUrl())
                .description(evidence.getDescription())
                .evidenceType(evidence.getEvidenceType())
                .uploadedAt(evidence.getUploadedAt())
                .build();
    }
}
