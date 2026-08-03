package com.uko.eaas.communication.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class EvidenceResponse {

    private UUID id;
    private UUID disputeId;
    private UUID uploadedBy;

    private String fileName;
    private String originalName;
    private String fileType;
    private Long fileSizeBytes;
    private String mimeType;

    private String url;
    private String description;
    private String evidenceType;

    private LocalDateTime uploadedAt;
}
