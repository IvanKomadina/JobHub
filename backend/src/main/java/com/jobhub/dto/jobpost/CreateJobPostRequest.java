package com.jobhub.dto.jobpost;

import com.jobhub.enums.EmploymentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class CreateJobPostRequest {

    @NotBlank(message = "Title cannot be empty")
    private String title;

    @NotBlank(message = "Description cannot be empty")
    private String description;

    private String requirements;

    @NotNull(message = "Employment type cannot be null")
    private EmploymentType employmentType;

    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private Long categoryId;
    private Long locationId;
    private LocalDateTime closesAt;
}
