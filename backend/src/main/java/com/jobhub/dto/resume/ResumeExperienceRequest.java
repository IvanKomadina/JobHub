package com.jobhub.dto.resume;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class ResumeExperienceRequest {

    @NotBlank(message = "Company cannot be empty")
    private String company;

    @NotBlank(message = "Position cannot be empty")
    private String position;

    private String location;
    private LocalDate startDate;
    private LocalDate endDate;
    private String description;
    private Integer sortOrder;
}
