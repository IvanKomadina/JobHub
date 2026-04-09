package com.jobhub.dto.application;

import com.jobhub.entity.ApplicationDocument;
import com.jobhub.enums.DocumentType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class DocumentResponse {

    private Long id;
    private String fileName;
    private String fileUrl;
    private DocumentType fileType;
    private LocalDateTime uploadedAt;

    public static DocumentResponse from(ApplicationDocument document) {
        return DocumentResponse.builder()
                .id(document.getId())
                .fileName(document.getFileName())
                .fileUrl(document.getFileUrl())
                .fileType(document.getFileType())
                .uploadedAt(document.getUploadedAt())
                .build();
    }
}
