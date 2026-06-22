package com.jobhub.service;

import com.jobhub.dto.employer.EmployerProfileResponse;
import com.jobhub.dto.employer.UpdateEmployerProfileRequest;
import com.jobhub.entity.Employer;
import com.jobhub.entity.User;
import com.jobhub.exception.ResourceNotFoundException;
import com.jobhub.repository.EmployerRepository;
import com.jobhub.repository.UserRepository;
import com.jobhub.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployerService {

    private final EmployerRepository employerRepository;
    private final UserRepository userRepository;
    private final StorageService storageService;

    @Transactional(readOnly = true)
    public EmployerProfileResponse getProfile(AuthenticatedUser currentUser) {
        Employer employer = employerRepository.findByUser_Id(currentUser.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Employer profile not found"));
        return EmployerProfileResponse.from(employer);
    }

    @Transactional
    public EmployerProfileResponse updateProfile(UpdateEmployerProfileRequest request,
                                                 AuthenticatedUser currentUser) {
        Employer employer = employerRepository.findByUser_Id(currentUser.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Employer profile not found"));
        employer.updateProfile(
                request.getCompanyName(),
                request.getIndustry(),
                request.getWebsite(),
                request.getCity(),
                request.getCountry(),
                request.getDescription()
        );
        employerRepository.save(employer);
        return EmployerProfileResponse.from(employer);
    }

    @Transactional
    public void deleteAccount(AuthenticatedUser currentUser) {
        User user = userRepository.findById(currentUser.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        userRepository.delete(user);
    }

    @Transactional
    public EmployerProfileResponse updateLogo(MultipartFile file,
                                              AuthenticatedUser currentUser) {
        Employer employer = employerRepository.findByUser_Id(currentUser.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Employer profile not found"));

        // Delete old logo if exists
        if (employer.getLogoUrl() != null) {
            try {
                storageService.delete(employer.getLogoUrl());
            } catch (Exception e) {
                log.warn("Failed to delete old logo: {}", e.getMessage());
            }
        }

        String logoUrl = storageService.upload(file, "logos");
        employer.updateLogo(logoUrl);
        employerRepository.save(employer);
        return EmployerProfileResponse.from(employer);
    }
}