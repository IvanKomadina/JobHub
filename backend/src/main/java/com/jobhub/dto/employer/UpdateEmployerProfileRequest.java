package com.jobhub.dto.employer;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class UpdateEmployerProfileRequest {

    @NotBlank(message = "Company name cannot be empty")
    private String companyName;

    private String industry;
    private String website;
    private String location;
    private String description;
}