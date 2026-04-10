package com.jobhub.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "resume_experience")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ResumeExperience {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    @Column(nullable = false)
    private String company;

    @Column(nullable = false)
    private String position;

    private String location;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Builder(access = AccessLevel.PRIVATE)
    private ResumeExperience(Resume resume, String company, String position,
                             String location, LocalDate startDate, LocalDate endDate,
                             String description, Integer sortOrder) {
        this.resume = resume;
        this.company = company;
        this.position = position;
        this.location = location;
        this.startDate = startDate;
        this.endDate = endDate;
        this.description = description;
        this.sortOrder = sortOrder != null ? sortOrder : 0;
    }

    public static ResumeExperience create(Resume resume, String company, String position,
                                          String location, LocalDate startDate,
                                          LocalDate endDate, String description,
                                          Integer sortOrder) {

        if (resume == null) throw new IllegalArgumentException("Resume cannot be null");
        if (company == null || company.isBlank()) throw new IllegalArgumentException("Company cannot be empty");
        if (position == null || position.isBlank()) throw new IllegalArgumentException("Position cannot be empty");
        if (startDate != null && endDate != null && startDate.isAfter(endDate))
            throw new IllegalArgumentException("Start date cannot be after end date");

        return ResumeExperience.builder()
                .resume(resume)
                .company(company)
                .position(position)
                .location(location)
                .startDate(startDate)
                .endDate(endDate)
                .description(description)
                .sortOrder(sortOrder)
                .build();
    }

    public void update(String company, String position, String location,
                       LocalDate startDate, LocalDate endDate,
                       String description, Integer sortOrder) {
        if (company == null || company.isBlank()) throw new IllegalArgumentException("Company cannot be empty");
        if (position == null || position.isBlank()) throw new IllegalArgumentException("Position cannot be empty");
        if (startDate != null && endDate != null && startDate.isAfter(endDate))
            throw new IllegalArgumentException("Start date cannot be after end date");
        this.company = company;
        this.position = position;
        this.location = location;
        this.startDate = startDate;
        this.endDate = endDate;
        this.description = description;
        this.sortOrder = sortOrder != null ? sortOrder : 0;
    }

    public boolean isCurrent() {
        LocalDate today = LocalDate.now();
        return startDate != null &&
                !startDate.isAfter(today) &&
                (endDate == null || endDate.isAfter(today));
    }
}
