package com.jobhub.service;

import com.jobhub.dto.application.AssessmentResponse;
import com.jobhub.dto.application.UpdateAssessmentNotesRequest;
import com.jobhub.entity.ApplicationAssessment;
import com.jobhub.entity.Employer;
import com.jobhub.entity.JobPost;
import com.jobhub.exception.AccessDeniedException;
import com.jobhub.exception.ResourceNotFoundException;
import com.jobhub.repository.ApplicationAssessmentRepository;
import com.jobhub.repository.ApplicationRepository;
import com.jobhub.repository.EmployerRepository;
import com.jobhub.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AssessmentService {

    private final ApplicationAssessmentRepository assessmentRepository;
    private final ApplicationRepository applicationRepository;
    private final EmployerRepository employerRepository;

    @Transactional(readOnly = true)
    public AssessmentResponse getAssessment(Long applicationId,
                                            AuthenticatedUser currentUser) {
        verifyEmployerOwnership(applicationId, currentUser.getUserId());
        ApplicationAssessment assessment = assessmentRepository
                .findByApplication_Id(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Assessment not found — generate one first"));
        return AssessmentResponse.from(assessment);
    }

    @Transactional
    public AssessmentResponse updateNotes(Long applicationId,
                                          UpdateAssessmentNotesRequest request,
                                          AuthenticatedUser currentUser) {
        verifyEmployerOwnership(applicationId, currentUser.getUserId());
        ApplicationAssessment assessment = assessmentRepository
                .findByApplication_Id(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Assessment not found — generate one first"));
        assessment.updateEmployerNotes(request.getEmployerNotes());
        assessmentRepository.save(assessment);
        return AssessmentResponse.from(assessment);
    }

    private void verifyEmployerOwnership(Long applicationId, Long userId) {
        var application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

        Employer employer = employerRepository.findByUser_Id(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Employer not found"));

        JobPost post = application.getJobPost();

        if (!post.getEmployer().getId().equals(employer.getId())) {
            throw new AccessDeniedException(
                    "You do not have permission to access this assessment");
        }
    }
}