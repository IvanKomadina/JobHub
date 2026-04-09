package com.jobhub.dto.application;

import com.jobhub.entity.Application;
import com.jobhub.enums.ApplicationStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ApplicationResponse {

    private Long id;
    private ApplicationStatus status;
    private String coverLetter;
    private LocalDateTime appliedAt;
    private LocalDateTime updatedAt;

    // Job post info
    private Long jobPostId;
    private String jobPostTitle;
    private String companyName;
    private String companyLogo;

    // Candidate info
    private Long candidateId;
    private String candidateFirstName;
    private String candidateLastName;
    private String candidateEmail;

    public static ApplicationResponse from(Application application) {
        return ApplicationResponse.builder()
                .id(application.getId())
                .status(application.getStatus())
                .coverLetter(application.getCoverLetter())
                .appliedAt(application.getAppliedAt())
                .updatedAt(application.getUpdatedAt())
                .jobPostId(application.getJobPost().getId())
                .jobPostTitle(application.getJobPost().getTitle())
                .companyName(application.getJobPost().getEmployer().getCompanyName())
                .companyLogo(application.getJobPost().getEmployer().getLogoUrl())
                .candidateId(application.getCandidate().getId())
                .candidateFirstName(application.getCandidate().getFirstName())
                .candidateLastName(application.getCandidate().getLastName())
                .candidateEmail(application.getCandidate().getUser().getEmail())
                .build();
    }
}
