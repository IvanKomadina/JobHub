package com.jobhub.dto.employer;

import com.jobhub.entity.Employer;
import com.jobhub.enums.EmployerStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EmployerProfileResponse {

    private Long id;
    private String email;
    private String companyName;
    private String industry;
    private String website;
    private String location;
    private String logoUrl;
    private String description;
    private EmployerStatus status;

    public static EmployerProfileResponse from(Employer employer) {
        return EmployerProfileResponse.builder()
                .id(employer.getId())
                .email(employer.getUser().getEmail())
                .companyName(employer.getCompanyName())
                .industry(employer.getIndustry())
                .website(employer.getWebsite())
                .location(employer.getLocation())
                .logoUrl(employer.getLogoUrl())
                .description(employer.getDescription())
                .status(employer.getStatus())
                .build();
    }
}