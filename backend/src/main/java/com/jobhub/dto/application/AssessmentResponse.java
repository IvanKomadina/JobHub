package com.jobhub.dto.application;

import com.jobhub.entity.ApplicationAssessment;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class AssessmentResponse {

    private Long id;
    private Long applicationId;
    private BigDecimal matchScore;
    private String employerNotes;
    private LocalDateTime assessedAt;

    public static AssessmentResponse from(ApplicationAssessment assessment) {
        return AssessmentResponse.builder()
                .id(assessment.getId())
                .applicationId(assessment.getApplication().getId())
                .matchScore(assessment.getMatchScore())
                .employerNotes(assessment.getEmployerNotes())
                .assessedAt(assessment.getAssessedAt())
                .build();
    }
}
