package com.jobhub.dto.admin;

import com.jobhub.entity.User;
import com.jobhub.enums.UserRole;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AdminUserResponse {

    private Long id;
    private String email;
    private UserRole role;
    private boolean isActive;
    private LocalDateTime createdAt;

    // Candidate specific
    private String firstName;
    private String lastName;

    // Employer specific
    private String companyName;
    private String employerStatus;

    public static AdminUserResponse from(User user) {
        AdminUserResponse.AdminUserResponseBuilder builder = AdminUserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt());

        if (user.getRole() == UserRole.CANDIDATE && user.getCandidate() != null) {
            builder.firstName(user.getCandidate().getFirstName());
            builder.lastName(user.getCandidate().getLastName());
        }

        if (user.getRole() == UserRole.EMPLOYER && user.getEmployer() != null) {
            builder.companyName(user.getEmployer().getCompanyName());
            builder.employerStatus(user.getEmployer().getStatus().name());
        }

        return builder.build();
    }
}
