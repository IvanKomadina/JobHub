package com.jobhub.dto.candidate;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class UpdateCandidateProfileRequest {

    @NotBlank(message = "First name cannot be empty")
    private String firstName;

    @NotBlank(message = "Last name cannot be empty")
    private String lastName;

    private String phone;
    private String location;
    private String bio;
}