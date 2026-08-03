package com.uko.eaas.communication.service;

import com.uko.eaas.communication.dto.*;
import com.uko.eaas.communication.model.enums.DisputeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface DisputeService {

    DisputeResponse createDispute(CreateDisputeRequest request);

    DisputeResponse getDispute(String reference);

    DisputeResponse getDisputeByEscrow(String escrowReference);

    Page<DisputeResponse> listDisputes(UUID userId, Pageable pageable);

    Page<DisputeResponse> listActiveDisputes(Pageable pageable);

    DisputeResponse resolveDispute(String reference, ResolveDisputeRequest request, UUID resolvedBy);

    DisputeResponse updateDisputeStatus(String reference, DisputeStatus status);

    DisputeMessageResponse addMessage(DisputeMessageRequest request);

    Page<DisputeMessageResponse> getMessages(UUID disputeId, Pageable pageable);

    void markMessagesAsRead(UUID disputeId, String readerType);

    EvidenceResponse uploadEvidence(UUID disputeId, UUID uploadedBy, MultipartFile file, String description, String evidenceType);

    EvidenceResponse uploadEvidence(UUID disputeId, UUID uploadedBy, String fileName, String contentType, byte[] content, String description, String evidenceType);

    void deleteEvidence(UUID evidenceId, UUID deletedBy);
}
