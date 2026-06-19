package com.jobhub.entity;

import com.jobhub.enums.AssessmentStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;
import com.jobhub.enums.RecommendationType;

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

    @Column(name = "semantic_score", precision = 5, scale = 2)
    private BigDecimal semanticScore;

    @Column(name = "skills_score", precision = 5, scale = 2)
    private BigDecimal skillsScore;

    @Column(name = "experience_score", precision = 5, scale = 2)
    private BigDecimal experienceScore;

    @Column(name = "education_score", precision = 5, scale = 2)
    private BigDecimal educationScore;

    @Column(name = "skills_match", columnDefinition = "TEXT")
    private String skillsMatch;

    @Column(name = "skills_gap", columnDefinition = "TEXT")
    private String skillsGap;

    @Column(name = "experience_assessment", columnDefinition = "TEXT")
    private String experienceAssessment;

    @Column(name = "education_assessment", columnDefinition = "TEXT")
    private String educationAssessment;

    @Column(name = "explanation", columnDefinition = "TEXT")
    private String explanation;

    @Enumerated(EnumType.STRING)
    @Column(name = "recommendation", length = 50)
    private RecommendationType recommendation;

    @Column(name = "employer_notes", columnDefinition = "TEXT")
    private String employerNotes;

    @Enumerated(EnumType.STRING)
    @Column(name = "assessment_status", nullable = false, length = 50)
    private AssessmentStatus assessmentStatus;

    @UpdateTimestamp
    @Column(name = "assessed_at", nullable = false)
    private LocalDateTime assessedAt;

    @Builder(access = AccessLevel.PRIVATE)
    private ApplicationAssessment(Application application, BigDecimal matchScore,
                                  BigDecimal semanticScore, BigDecimal skillsScore,
                                  BigDecimal experienceScore, BigDecimal educationScore,
                                  String skillsMatch, String skillsGap,
                                  String experienceAssessment, String educationAssessment,
                                  String explanation, RecommendationType recommendation,
                                  AssessmentStatus assessmentStatus) {
        this.application = application;
        this.matchScore = matchScore;
        this.semanticScore = semanticScore;
        this.skillsScore = skillsScore;
        this.experienceScore = experienceScore;
        this.educationScore = educationScore;
        this.skillsMatch = skillsMatch;
        this.skillsGap = skillsGap;
        this.experienceAssessment = experienceAssessment;
        this.educationAssessment = educationAssessment;
        this.explanation = explanation;
        this.recommendation = recommendation;
        this.assessmentStatus = assessmentStatus;;
    }

    public static ApplicationAssessment createPending(Application application) {
        if (application == null) throw new IllegalArgumentException("Application cannot be null");
        return ApplicationAssessment.builder()
                .application(application)
                .assessmentStatus(AssessmentStatus.PENDING)
                .build();
    }

    public static ApplicationAssessment create(Application application,
                                               BigDecimal matchScore,
                                               BigDecimal semanticScore,
                                               BigDecimal skillsScore,
                                               BigDecimal experienceScore,
                                               BigDecimal educationScore,
                                               String skillsMatch,
                                               String skillsGap,
                                               String experienceAssessment,
                                               String educationAssessment,
                                               String explanation,
                                               RecommendationType recommendation) {
        if (application == null) throw new IllegalArgumentException("Application cannot be null");
        validateScore(matchScore, "Match score");
        validateScore(semanticScore, "Semantic score");
        validateScore(skillsScore, "Skills score");
        validateScore(experienceScore, "Experience score");
        validateScore(educationScore, "Education score");
        return ApplicationAssessment.builder()
                .application(application)
                .matchScore(matchScore)
                .semanticScore(semanticScore)
                .skillsScore(skillsScore)
                .experienceScore(experienceScore)
                .educationScore(educationScore)
                .skillsMatch(skillsMatch)
                .skillsGap(skillsGap)
                .experienceAssessment(experienceAssessment)
                .educationAssessment(educationAssessment)
                .explanation(explanation)
                .recommendation(recommendation)
                .build();
    }

    public void updateEmployerNotes(String notes) {
        this.employerNotes = notes;
    }

    public void regenerate(BigDecimal matchScore, BigDecimal semanticScore,
                           BigDecimal skillsScore, BigDecimal experienceScore,
                           BigDecimal educationScore, String skillsMatch,
                           String skillsGap, String experienceAssessment,
                           String educationAssessment, String explanation,
                           RecommendationType recommendation) {
        validateScore(matchScore, "Match score");
        this.matchScore = matchScore;
        this.semanticScore = semanticScore;
        this.skillsScore = skillsScore;
        this.experienceScore = experienceScore;
        this.educationScore = educationScore;
        this.skillsMatch = skillsMatch;
        this.skillsGap = skillsGap;
        this.experienceAssessment = experienceAssessment;
        this.educationAssessment = educationAssessment;
        this.explanation = explanation;
        this.recommendation = recommendation;
    }

    private static void validateScore(BigDecimal score, String name) {
        if (score == null) return;
        if (score.compareTo(BigDecimal.ZERO) < 0 ||
                score.compareTo(new BigDecimal("100")) > 0)
            throw new IllegalArgumentException(name + " must be between 0 and 100");
    }

    public void markGenerating() {
        this.assessmentStatus = AssessmentStatus.GENERATING;
    }

    public void markFailed() {
        this.assessmentStatus = AssessmentStatus.FAILED;
    }

    public void markCompleted() {
        this.assessmentStatus = AssessmentStatus.COMPLETED;
    }
}
