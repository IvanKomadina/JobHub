package com.jobhub.dto.resume;

import com.jobhub.entity.ResumeExperience;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class ResumeExperienceResponse {

    private Long id;
    private String company;
    private String position;
    private String location;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean current;
    private String description;
    private Integer sortOrder;

    public static ResumeExperienceResponse from(ResumeExperience experience) {
        return ResumeExperienceResponse.builder()
                .id(experience.getId())
                .company(experience.getCompany())
                .position(experience.getPosition())
                .location(experience.getLocation())
                .startDate(experience.getStartDate())
                .endDate(experience.getEndDate())
                .current(experience.isCurrent())
                .description(experience.getDescription())
                .sortOrder(experience.getSortOrder())
                .build();
    }
}
