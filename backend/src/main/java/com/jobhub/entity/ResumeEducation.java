package com.jobhub.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "resume_education")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ResumeEducation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    @Column(nullable = false, length = 255)
    private String institution;

    @Column(length = 255)
    private String degree;

    @Column(name = "field_of_study", length = 255)
    private String fieldOfStudy;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Builder(access = AccessLevel.PRIVATE)
    private ResumeEducation(Resume resume, String institution, String degree,
                            String fieldOfStudy, LocalDate startDate, LocalDate endDate,
                            String description, Integer sortOrder) {
        this.resume = resume;
        this.institution = institution;
        this.degree = degree;
        this.fieldOfStudy = fieldOfStudy;
        this.startDate = startDate;
        this.endDate = endDate;
        this.description = description;
        this.sortOrder = sortOrder != null ? sortOrder : 0;
    }

    public static ResumeEducation create(Resume resume, String institution, String degree,
                                         String fieldOfStudy, LocalDate startDate,
                                         LocalDate endDate, String description,
                                         Integer sortOrder) {

        if (resume == null) throw new IllegalArgumentException("Resume cannot be null");
        if (institution == null || institution.isBlank()) throw new IllegalArgumentException("Institution cannot be empty");
        if (startDate != null && endDate != null && startDate.isAfter(endDate))
            throw new IllegalArgumentException("Start date cannot be after end date");

        return ResumeEducation.builder()
                .resume(resume)
                .institution(institution)
                .degree(degree)
                .fieldOfStudy(fieldOfStudy)
                .startDate(startDate)
                .endDate(endDate)
                .description(description)
                .sortOrder(sortOrder)
                .build();
    }

    public void update(String institution, String degree, String fieldOfStudy,
                       LocalDate startDate, LocalDate endDate,
                       String description, Integer sortOrder) {
        if (institution == null || institution.isBlank()) throw new IllegalArgumentException("Institution cannot be empty");
        if (startDate != null && endDate != null && startDate.isAfter(endDate))
            throw new IllegalArgumentException("Start date cannot be after end date");
        this.institution = institution;
        this.degree = degree;
        this.fieldOfStudy = fieldOfStudy;
        this.startDate = startDate;
        this.endDate = endDate;
        this.description = description;
        this.sortOrder = sortOrder != null ? sortOrder : 0;
    }
}
