package com.jobhub.entity;

import com.jobhub.enums.EmploymentType;
import com.jobhub.enums.PostStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "job_posts",
        indexes = {
                @Index(name = "idx_job_posts_status", columnList = "status"),
                @Index(name = "idx_job_posts_category_id", columnList = "category_id"),
                @Index(name = "idx_job_posts_location_id", columnList = "location_id"),
                @Index(name = "idx_job_posts_employer_id", columnList = "employer_id"),
                @Index(name = "idx_job_posts_published_at", columnList = "published_at DESC")
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JobPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employer_id", nullable = false)
    private Employer employer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String requirements;

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_type", length = 50)
    private EmploymentType employmentType;

    @Column(name = "salary_min", precision = 12, scale = 2)
    private BigDecimal salaryMin;

    @Column(name = "salary_max", precision = 12, scale = 2)
    private BigDecimal salaryMax;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private PostStatus status;

    @Column(name = "published_at", nullable = false)
    private LocalDateTime publishedAt;

    @Column(name = "closes_at")
    private LocalDateTime closesAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder(access = AccessLevel.PRIVATE)
    private JobPost(Employer employer, Category category, Location location, String title,
                    String description, String requirements, EmploymentType employmentType, BigDecimal salaryMin,
                    BigDecimal salaryMax, LocalDateTime closesAt) {
        this.employer = employer;
        this.category = category;
        this.location = location;
        this.title = title;
        this.description = description;
        this.requirements = requirements;
        this.employmentType = employmentType;
        this.salaryMin = salaryMin;
        this.salaryMax = salaryMax;
        this.status = PostStatus.ACTIVE;
        this.publishedAt = LocalDateTime.now();
        this.closesAt = closesAt;
    }

    public static JobPost create(Employer employer, Category category, Location location, String title,
                                 String description, String requirements, EmploymentType employmentType, BigDecimal salaryMin,
                                 BigDecimal salaryMax, LocalDateTime closesAt) {

        if (employer == null) throw new IllegalArgumentException("Employer cannot be null");
        if (title == null || title.isBlank()) throw new IllegalArgumentException("Title cannot be empty");
        if (description == null || description.isBlank()) throw new IllegalArgumentException("Description cannot be empty");
        if (salaryMin != null && salaryMax != null && salaryMin.compareTo(salaryMax) > 0)
            throw new IllegalArgumentException("Salary min cannot be greater than salary max");
        if (salaryMin != null && salaryMin.signum() < 0) throw new IllegalArgumentException("Salary cannot be negative");
        if (salaryMax != null && salaryMax.signum() < 0) throw new IllegalArgumentException("Salary cannot be negative");

        return JobPost.builder()
                .employer(employer)
                .category(category)
                .location(location)
                .title(title)
                .description(description)
                .requirements(requirements)
                .employmentType(employmentType)
                .salaryMin(salaryMin)
                .salaryMax(salaryMax)
                .closesAt(closesAt)
                .build();
    }

    public void update(Category category, Location location, String title,
                       String description, String requirements, EmploymentType employmentType,
                       BigDecimal salaryMin, BigDecimal salaryMax, LocalDateTime closesAt) {

        if (title == null || title.isBlank()) throw new IllegalArgumentException("Title cannot be empty");
        if (description == null || description.isBlank()) throw new IllegalArgumentException("Description cannot be empty");
        if (salaryMin != null && salaryMax != null && salaryMin.compareTo(salaryMax) > 0)
            throw new IllegalArgumentException("Salary min cannot be greater than salary max");
        if (this.status != PostStatus.ACTIVE) throw new IllegalStateException("Only active posts can be edited");

        this.category = category;
        this.location = location;
        this.title = title;
        this.description = description;
        this.requirements = requirements;
        this.employmentType = employmentType;
        this.salaryMin = salaryMin;
        this.salaryMax = salaryMax;
        this.closesAt = closesAt;
    }

    public void close() {
        if (this.status != PostStatus.ACTIVE) throw new IllegalStateException("Only active posts can be closed");
        this.status = PostStatus.CLOSED;
    }

    public void delete() {
        if (this.status == PostStatus.DELETED) throw new IllegalStateException("Post is already deleted");
        this.status = PostStatus.DELETED;
    }
}