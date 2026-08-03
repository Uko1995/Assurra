package com.uko.eaas.identity.dto;

import com.uko.eaas.identity.model.enums.DocumentType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class KycDocumentUploadResponse {

    private UUID id;
    private UUID merchantId;
    private DocumentType documentType;
    private String fileUrl;
    private String fileName;
    private Integer fileSizeKb;
    private String mimeType;
    private LocalDateTime uploadedAt;
}
