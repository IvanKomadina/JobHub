package com.jobhub.entity;

import com.jobhub.enums.LanguageLevel;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "resume_languages",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"resume_id", "language_name"}
        ))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ResumeLanguage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    @Column(name = "language_name", nullable = false, length = 100)
    private String languageName;

    @Enumerated(EnumType.STRING)
    @Column(name = "language_level", length = 50)
    private LanguageLevel languageLevel;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Builder(access = AccessLevel.PRIVATE)
    private ResumeLanguage(Resume resume, String languageName,
                           LanguageLevel languageLevel, Integer sortOrder) {
        this.resume = resume;
        this.languageName = capitalize(languageName);
        this.languageLevel = languageLevel;
        this.sortOrder = sortOrder != null ? sortOrder : 0;
    }

    public static ResumeLanguage create(Resume resume, String languageName,
                                        LanguageLevel languageLevel, Integer sortOrder) {
        if (resume == null) throw new IllegalArgumentException("Resume cannot be null");
        if (languageName == null || languageName.isBlank()) throw new IllegalArgumentException("Language name cannot be empty");
        return ResumeLanguage.builder()
                .resume(resume)
                .languageName(languageName)
                .languageLevel(languageLevel)
                .sortOrder(sortOrder)
                .build();
    }

    public void update(String languageName, LanguageLevel languageLevel, Integer sortOrder) {
        if (languageName == null || languageName.isBlank()) throw new IllegalArgumentException("Language name cannot be empty");
        this.languageName = capitalize(languageName);
        this.languageLevel = languageLevel;
        this.sortOrder = sortOrder != null ? sortOrder : 0;
    }

    private static String capitalize(String languageName) {
        String trimmed = languageName.trim();
        return trimmed.substring(0, 1).toUpperCase() + trimmed.substring(1).toLowerCase();
    }
}
