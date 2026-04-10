package com.jobhub.dto.resume;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class ResumeEducationRequest {

    @NotBlank(message = "Institution cannot be empty")
    private String institution;
    private String degree;
    private String fieldOfStudy;
    private LocalDate startDate;
    private LocalDate endDate;
    private String description;
    private Integer sortOrder;
}
