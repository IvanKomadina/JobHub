package com.jobhub.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.net.URI;
import java.util.UUID;

@Service
public class StorageService {

    private final S3Client s3Client;
    private final String bucketName;
    private final String publicUrl;

    public StorageService(
            @Value("${r2.account-id}") String accountId,
            @Value("${r2.access-key-id}") String accessKeyId,
            @Value("${r2.secret-access-key}") String secretAccessKey,
            @Value("${r2.bucket-name}") String bucketName,
            @Value("${r2.public-url}") String publicUrl) {

        this.bucketName = bucketName;
        this.publicUrl = publicUrl;

        this.s3Client = S3Client.builder()
                .endpointOverride(URI.create(
                        "https://" + accountId + ".r2.cloudflarestorage.com"))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKeyId, secretAccessKey)))
                .region(Region.of("auto"))
                .build();
    }

    public String upload(MultipartFile file, String folder) {
        validateFile(file);

        String key = folder + "/" + UUID.randomUUID() + "_" + sanitizeFileName(file.getOriginalFilename());

        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromBytes(file.getBytes()));
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file: " + e.getMessage());
        }

        return publicUrl + "/" + key;
    }

    public void delete(String fileUrl) {
        String key = extractKeyFromUrl(fileUrl);

        DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        s3Client.deleteObject(deleteRequest);
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty())
            throw new IllegalArgumentException("File cannot be empty");
        if (file.getSize() > 10 * 1024 * 1024)
            throw new IllegalArgumentException("File size cannot exceed 10MB");

        String contentType = file.getContentType();
        if (contentType == null || !isAllowedContentType(contentType))
            throw new IllegalArgumentException("File type not allowed. Allowed types: PDF, DOC, DOCX, JPG, PNG");
    }

    private boolean isAllowedContentType(String contentType) {
        return contentType.equals("application/pdf") ||
                contentType.equals("application/msword") ||
                contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document") ||
                contentType.equals("image/jpeg") ||
                contentType.equals("image/png");
    }

    private String sanitizeFileName(String fileName) {
        if (fileName == null)
            return "file";
        return fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private String extractKeyFromUrl(String fileUrl) {
        return fileUrl.replace(publicUrl + "/", "");
    }
}
