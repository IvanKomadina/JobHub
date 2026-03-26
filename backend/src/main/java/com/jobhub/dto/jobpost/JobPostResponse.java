package com.jobhub.dto.jobpost;

import com.jobhub.entity.JobPost;
import com.jobhub.enums.EmploymentType;
import com.jobhub.enums.PostStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class JobPostResponse {

    private Long id;
    private String title;
    private String description;
    private String requirements;
    private EmploymentType employmentType;
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private PostStatus status;
    private LocalDateTime publishedAt;
    private LocalDateTime closesAt;

    // Employer info
    private Long employerId;
    private String companyName;
    private String companyLogo;

    // Category info
    private Long categoryId;
    private String categoryName;

    // Location info
    private Long locationId;
    private String city;
    private String country;

    public static JobPostResponse from(JobPost jobPost) {
        return JobPostResponse.builder()
                .id(jobPost.getId())
                .title(jobPost.getTitle())
                .description(jobPost.getDescription())
                .requirements(jobPost.getRequirements())
                .employmentType(jobPost.getEmploymentType())
                .salaryMin(jobPost.getSalaryMin())
                .salaryMax(jobPost.getSalaryMax())
                .status(jobPost.getStatus())
                .publishedAt(jobPost.getPublishedAt())
                .closesAt(jobPost.getClosesAt())
                .employerId(jobPost.getEmployer().getId())
                .companyName(jobPost.getEmployer().getCompanyName())
                .companyLogo(jobPost.getEmployer().getLogoUrl())
                .categoryId(jobPost.getCategory() != null ? jobPost.getCategory().getId() : null)
                .categoryName(jobPost.getCategory() != null ? jobPost.getCategory().getName() : null)
                .locationId(jobPost.getLocation() != null ? jobPost.getLocation().getId() : null)
                .city(jobPost.getLocation() != null ? jobPost.getLocation().getCity() : null)
                .country(jobPost.getLocation() != null ? jobPost.getLocation().getCountry() : null)
                .build();
    }
}
