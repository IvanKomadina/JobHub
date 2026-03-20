package com.jobhub.entity;

import com.jobhub.enums.EmployerStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "employers")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Employer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "company_name", nullable = false, length = 255)
    private String companyName;

    @Column(length = 100)
    private String industry;

    @Column(length = 512)
    private String website;

    @Column(length = 255)
    private String location;

    @Column(name = "logo_url", length = 512)
    private String logoUrl;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private EmployerStatus status = EmployerStatus.PENDING;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder(access = AccessLevel.PRIVATE)
    private Employer(User user, String companyName) {
        this.user = user;
        this.companyName = companyName;
    }

    public static Employer create(User user, String companyName) {
        if (user == null) throw new IllegalArgumentException("User cannot be null");
        if (companyName == null || companyName.isBlank()) throw new IllegalArgumentException("Company name cannot be empty");
        return Employer.builder()
                .user(user)
                .companyName(companyName)
                .build();
    }

    public void updateProfile(String companyName, String industry, String website, String location,
                              String description) {
        if (companyName == null || companyName.isBlank()) throw new IllegalArgumentException("Company name cannot be empty");
        this.companyName = companyName;
        this.industry = industry;
        this.website = website;
        this.location = location;
        this.description = description;
    }

    public void updateLogo(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public void approve() {
        this.status = EmployerStatus.APPROVED;
    }

    public void reject() {
        this.status = EmployerStatus.REJECTED;
    }
}
