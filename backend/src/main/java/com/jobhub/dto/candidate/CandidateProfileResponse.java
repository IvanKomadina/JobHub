package com.jobhub.dto.candidate;

import com.jobhub.entity.Candidate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CandidateProfileResponse {

    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String location;
    private String profilePicture;
    private String bio;

    public static CandidateProfileResponse from(Candidate candidate) {
        return CandidateProfileResponse.builder()
                .id(candidate.getId())
                .email(candidate.getUser().getEmail())
                .firstName(candidate.getFirstName())
                .lastName(candidate.getLastName())
                .phone(candidate.getPhone())
                .location(candidate.getLocation())
                .profilePicture(candidate.getProfilePicture())
                .bio(candidate.getBio())
                .build();
    }
}