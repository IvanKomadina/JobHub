package com.jobhub.entity;

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
@Table(name = "application_assessments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApplicationAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false, unique = true)
    private Application application;

    @Column(name = "match_score", precision = 5, scale = 2)
    private BigDecimal matchScore;

    @Column(name = "employer_notes", columnDefinition = "TEXT")
    private String employerNotes;

    @UpdateTimestamp
    @Column(name = "assessed_at", nullable = false)
    private LocalDateTime assessedAt;

    @Builder(access = AccessLevel.PROTECTED)
    private ApplicationAssessment(Application application, BigDecimal matchScore, String employerNotes) {
        this.application = application;
        this.matchScore = matchScore;
        this.employerNotes = employerNotes;
    }

    public static ApplicationAssessment create(Application application, BigDecimal matchScore, String employerNotes) {
        if (application == null) throw new IllegalArgumentException("Application cannot be null");
        validateMatchScore(matchScore);

        return ApplicationAssessment.builder()
                .application(application)
                .matchScore(matchScore)
                .employerNotes(employerNotes)
                .build();
    }

    public void update(BigDecimal matchScore, String employerNotes) {
        validateMatchScore(matchScore);
        this.matchScore = matchScore;
        this.employerNotes = employerNotes;
    }

    private static void validateMatchScore(BigDecimal matchScore) {
        if (matchScore == null) return;
        if (matchScore.compareTo(BigDecimal.ZERO) < 0 ||
                matchScore.compareTo(new BigDecimal("100")) > 0)
            throw new IllegalArgumentException("Match score must be between 0 and 100");
    }
}
