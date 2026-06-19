package com.jobhub.dto.application;

import com.jobhub.entity.ApplicationAssessment;
import com.jobhub.enums.AssessmentStatus;
import com.jobhub.enums.RecommendationType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Getter
@Builder
public class AssessmentResponse {

    private Long id;
    private Long applicationId;
    private BigDecimal matchScore;
    private BigDecimal semanticScore;
    private BigDecimal skillsScore;
    private BigDecimal experienceScore;
    private BigDecimal educationScore;
    private List<String> matchedSkills;
    private List<String> missingSkills;
    private String experienceAssessment;
    private String educationAssessment;
    private String explanation;
    private RecommendationType recommendation;
    private String employerNotes;
    private LocalDateTime assessedAt;
    private AssessmentStatus assessmentStatus;

    public static AssessmentResponse from(ApplicationAssessment assessment) {
        return AssessmentResponse.builder()
                .id(assessment.getId())
                .applicationId(assessment.getApplication().getId())
                .matchScore(assessment.getMatchScore())
                .semanticScore(assessment.getSemanticScore())
                .skillsScore(assessment.getSkillsScore())
                .experienceScore(assessment.getExperienceScore())
                .educationScore(assessment.getEducationScore())
                .matchedSkills(parseCommaSeparated(assessment.getSkillsMatch()))
                .missingSkills(parseCommaSeparated(assessment.getSkillsGap()))
                .experienceAssessment(assessment.getExperienceAssessment())
                .educationAssessment(assessment.getEducationAssessment())
                .explanation(assessment.getExplanation())
                .recommendation(assessment.getRecommendation())
                .employerNotes(assessment.getEmployerNotes())
                .assessedAt(assessment.getAssessedAt())
                .assessmentStatus(assessment.getAssessmentStatus())
                .build();
    }

    private static List<String> parseCommaSeparated(String value) {
        if (value == null || value.isBlank()) return List.of();
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
    }
}