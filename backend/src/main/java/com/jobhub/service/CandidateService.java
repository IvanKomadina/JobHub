package com.jobhub.service;

import com.jobhub.dto.candidate.CandidateProfileResponse;
import com.jobhub.dto.candidate.UpdateCandidateProfileRequest;
import com.jobhub.entity.Candidate;
import com.jobhub.entity.User;
import com.jobhub.enums.UserRole;
import com.jobhub.exception.ResourceNotFoundException;
import com.jobhub.repository.CandidateRepository;
import com.jobhub.repository.EmployerRepository;
import com.jobhub.repository.UserRepository;
import com.jobhub.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CandidateService {

    private final CandidateRepository candidateRepository;
    private final EmployerRepository employerRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public CandidateProfileResponse getProfile(AuthenticatedUser currentUser) {
        Candidate candidate = candidateRepository.findByUser_Id(currentUser.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found"));
        return CandidateProfileResponse.from(candidate);
    }

    @Transactional
    public CandidateProfileResponse updateProfile(UpdateCandidateProfileRequest request,
                                                  AuthenticatedUser currentUser) {
        Candidate candidate = candidateRepository.findByUser_Id(currentUser.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found"));
        candidate.updateProfile(
                request.getFirstName(),
                request.getLastName(),
                request.getPhone(),
                request.getLocation(),
                request.getBio()
        );
        candidateRepository.save(candidate);
        return CandidateProfileResponse.from(candidate);
    }

    @Transactional
    public void deleteAccount(AuthenticatedUser currentUser) {
        User user = userRepository.findById(currentUser.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getRole() == UserRole.CANDIDATE) {
            candidateRepository.findByUser_Id(user.getId())
                    .ifPresent(candidateRepository::delete);
        } else if (user.getRole() == UserRole.EMPLOYER) {
            employerRepository.findByUser_Id(user.getId())
                    .ifPresent(employerRepository::delete);
        }

        userRepository.delete(user);
    }
}
