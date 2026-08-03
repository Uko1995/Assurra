package com.uko.eaas.communication.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.uko.eaas.communication.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloudinaryStorageService implements StorageService {

    private final Cloudinary cloudinary;

    @Override
    public String uploadFile(String fileName, String contentType, byte[] content, String path) {
        try {
            Map<String, Object> options = ObjectUtils.asMap(
                    "public_id", buildPublicId(path, fileName),
                    "resource_type", "auto",
                    "use_filename", true,
                    "unique_filename", true
            );
            Map<?, ?> result = cloudinary.uploader().upload(content, options);
            String secureUrl = (String) result.get("secure_url");
            log.info("Uploaded file to Cloudinary: {}", secureUrl);
            return secureUrl;
        } catch (IOException e) {
            log.error("Failed to upload file to Cloudinary: {}", e.getMessage());
            throw new RuntimeException("File upload failed", e);
        }
    }

    @Override
    public String uploadFile(String fileName, String contentType, InputStream inputStream, long contentLength, String path) {
        try {
            byte[] content = inputStream.readAllBytes();
            return uploadFile(fileName, contentType, content, path);
        } catch (IOException e) {
            log.error("Failed to read input stream for Cloudinary upload: {}", e.getMessage());
            throw new RuntimeException("File upload failed", e);
        }
    }

    @Override
    public byte[] downloadFile(String fileKey) {
        // Cloudinary does not support direct byte download via SDK easily.
        // Typically, clients access files via the secure URL.
        throw new UnsupportedOperationException("Use the secure URL returned from upload to access the file directly.");
    }

    @Override
    public void deleteFile(String fileKey) {
        try {
            String publicId = extractPublicId(fileKey);
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            log.info("Deleted file from Cloudinary: {}", publicId);
        } catch (IOException e) {
            log.error("Failed to delete file from Cloudinary: {}", e.getMessage());
            throw new RuntimeException("File deletion failed", e);
        }
    }

    @Override
    public String generatePresignedUrl(String fileKey, int expirationMinutes) {
        // Cloudinary secure URLs are already accessible if the resource is public.
        // For restricted access, use signed URLs or transformation-based authentication.
        return fileKey;
    }

    @Override
    public boolean fileExists(String fileKey) {
        try {
            String publicId = extractPublicId(fileKey);
            Map<?, ?> result = cloudinary.api().resource(publicId, ObjectUtils.emptyMap());
            return result != null && result.get("public_id") != null;
        } catch (Exception e) {
            return false;
        }
    }

    private String buildPublicId(String path, String fileName) {
        String sanitized = fileName.replaceAll("[^a-zA-Z0-9.-]", "_");
        return path + "/" + System.currentTimeMillis() + "_" + sanitized;
    }

    private String extractPublicId(String fileKey) {
        // fileKey is the secure_url; extract the public_id portion after upload/
        if (fileKey == null || !fileKey.contains("/upload/")) {
            return fileKey;
        }
        String afterUpload = fileKey.substring(fileKey.indexOf("/upload/") + 8);
        // Remove version number if present (v1234567890/)
        if (afterUpload.matches("^v\\d+/.+")) {
            afterUpload = afterUpload.substring(afterUpload.indexOf('/') + 1);
        }
        // Remove file extension
        int lastDot = afterUpload.lastIndexOf('.');
        if (lastDot > 0) {
            afterUpload = afterUpload.substring(0, lastDot);
        }
        return afterUpload;
    }
}
