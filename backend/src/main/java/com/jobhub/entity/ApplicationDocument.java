package com.jobhub.entity;

import com.jobhub.enums.DocumentType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "application_documents")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApplicationDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "file_url", nullable = false, length = 512)
    private String fileUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "file_type", length = 50)
    private DocumentType fileType;

    @CreationTimestamp
    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    @Builder(access = AccessLevel.PRIVATE)
    private ApplicationDocument(Application application, String fileName,
                                String fileUrl, DocumentType fileType) {
        this.application = application;
        this.fileName = fileName;
        this.fileUrl = fileUrl;
        this.fileType = fileType;
    }

    public static ApplicationDocument create(Application application, String fileName,
                                     String fileUrl, DocumentType fileType) {
        if (application == null) throw new IllegalArgumentException("Application cannot be null");
        if (fileName == null || fileName.isBlank()) throw new IllegalArgumentException("File name cannot be empty");
        if (fileUrl == null || fileUrl.isBlank()) throw new IllegalArgumentException("File URL cannot be empty");
        if (fileType == null) throw new IllegalArgumentException("File type cannot be null");

        return ApplicationDocument.builder()
                .application(application)
                .fileName(fileName)
                .fileUrl(fileUrl)
                .fileType(fileType)
                .build();
    }
}
