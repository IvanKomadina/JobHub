package com.jobhub.dto.resume;

import com.jobhub.entity.ResumeEducation;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class ResumeEducationResponse {

    private Long id;
    private String institution;
    private String degree;
    private String fieldOfStudy;
    private LocalDate startDate;
    private LocalDate endDate;
    private String description;
    private Integer sortOrder;

    public static ResumeEducationResponse from(ResumeEducation education) {
        return ResumeEducationResponse.builder()
                .id(education.getId())
                .institution(education.getInstitution())
                .degree(education.getDegree())
                .fieldOfStudy(education.getFieldOfStudy())
                .startDate(education.getStartDate())
                .endDate(education.getEndDate())
                .description(education.getDescription())
                .sortOrder(education.getSortOrder())
                .build();
    }
}
