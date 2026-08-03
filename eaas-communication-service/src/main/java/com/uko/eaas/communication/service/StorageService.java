package com.uko.eaas.communication.service;

import java.io.InputStream;

public interface StorageService {

    String uploadFile(String fileName, String contentType, byte[] content, String path);

    String uploadFile(String fileName, String contentType, InputStream inputStream, long contentLength, String path);

    byte[] downloadFile(String fileKey);

    void deleteFile(String fileKey);

    String generatePresignedUrl(String fileKey, int expirationMinutes);

    boolean fileExists(String fileKey);
}
