package com.jobhub.service;

import com.jobhub.dto.application.*;
import com.jobhub.entity.*;
import com.jobhub.enums.ApplicationStatus;
import com.jobhub.enums.DocumentType;
import com.jobhub.exception.AccessDeniedException;
import com.jobhub.exception.ResourceNotFoundException;
import com.jobhub.repository.*;
import com.jobhub.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationAssessmentRepository assessmentRepository;
    private final JobPostRepository jobPostRepository;
    private final CandidateRepository candidateRepository;
    private final EmployerRepository employerRepository;
    private final ApplicationDocumentRepository documentRepository;
    private final StorageService storageService;

    // CANDIDATE

    /*@Transactional
    public ApplicationResponse apply(Long jobPostId, CreateApplicationRequest request,
                                     AuthenticatedUser currentUser) {
        JobPost jobPost = jobPostRepository.findById(jobPostId)
                .orElseThrow(() -> new ResourceNotFoundException("Job post not found"));
        Candidate candidate = getCandidateByUserId(currentUser.getUserId());

        if (applicationRepository.existsByJobPost_IdAndCandidate_Id(jobPostId, candidate.getId()))
            throw new IllegalStateException("You have already applied to this job post");

        Application application = Application.create(jobPost, candidate, request.getCoverLetter());
        try {
            applicationRepository.save(application);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("You have already applied to this job post");
        }
        return ApplicationResponse.from(application);
    }*/

    @Transactional
    public ApplicationResponse createDraft(Long jobPostId, CreateApplicationRequest request,
                                           AuthenticatedUser currentUser) {
        Candidate candidate = getCandidateByUserId(currentUser.getUserId());
        JobPost jobPost = jobPostRepository.findById(jobPostId)
                .orElseThrow(() -> new ResourceNotFoundException("Job post not found"));

        if (applicationRepository.
                existsByJobPost_IdAndCandidate_IdAndStatusNot(
                        jobPostId,
                        candidate.getId(),
                        ApplicationStatus.DRAFT))
            throw new IllegalStateException("You have already applied to this job post");

        Application application = Application.create(jobPost, candidate, request.getCoverLetter());
        applicationRepository.save(application);
        return ApplicationResponse.from(application);
    }

    @Transactional
    public ApplicationResponse submitApplication(Long applicationId, AuthenticatedUser currentUser) {
        Candidate candidate = getCandidateByUserId(currentUser.getUserId());
        Application application = getApplicationOwnedByCandidate(applicationId, candidate);

        boolean hasDocuments = documentRepository
                .findByApplication_Id(applicationId)
                .stream()
                .anyMatch(doc -> doc.getFileType() == DocumentType.RESUME);

        if (!hasDocuments)
            throw new IllegalStateException("You must upload a resume before submitting application");

        application.submit();
        //applicationRepository.save(application);
        return ApplicationResponse.from(application);
    }

    @Transactional
    public void withdraw(Long applicationId, AuthenticatedUser currentUser) {
        Candidate candidate = getCandidateByUserId(currentUser.getUserId());
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

        if (!application.getCandidate().getId().equals(candidate.getId()))
            throw new AccessDeniedException("You do not have permission to withdraw this application");

        application.withdraw();
        //applicationRepository.save(application);
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> getMyApplications(AuthenticatedUser currentUser) {
        Candidate candidate = getCandidateByUserId(currentUser.getUserId());
        return applicationRepository.findByCandidate_Id(candidate.getId())
                .stream()
                .map(ApplicationResponse::from)
                .toList();
    }

    @Transactional
    public DocumentResponse uploadDocument(Long applicationId,
                                           MultipartFile file,
                                           DocumentType documentType,
                                           AuthenticatedUser currentUser) {
        Candidate candidate = getCandidateByUserId(currentUser.getUserId());
        Application application = getApplicationOwnedByCandidate(applicationId, candidate);

        if (application.getStatus() != ApplicationStatus.DRAFT)
            throw new IllegalStateException("Documents can only be added to draft applications");

        String fileUrl = storageService.upload(file, "applications/" + applicationId);

        ApplicationDocument document = ApplicationDocument.create(
                application,
                file.getOriginalFilename(),
                fileUrl,
                documentType
        );

        documentRepository.save(document);
        return DocumentResponse.from(document);
    }

    @Transactional
    public void deleteDocument(Long applicationId, Long documentId, AuthenticatedUser currentUser) {
        Candidate candidate = getCandidateByUserId(currentUser.getUserId());
        Application application = getApplicationOwnedByCandidate(applicationId, candidate);

        if (application.getStatus() != ApplicationStatus.DRAFT)
            throw new IllegalStateException("Documents can only be removed from draft applications");

        ApplicationDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));

        if (!document.getApplication().getId().equals(applicationId))
            throw new AccessDeniedException("Document does not belong to this application");

        storageService.delete(document.getFileUrl());
        documentRepository.delete(document);
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> getDocuments(Long applicationId, AuthenticatedUser currentUser) {
        Candidate candidate = getCandidateByUserId(currentUser.getUserId());
        getApplicationOwnedByCandidate(applicationId, candidate);
        return documentRepository.findByApplication_Id(applicationId)
                .stream()
                .map(DocumentResponse::from)
                .toList();
    }

    // EMPLOYER

    @Transactional(readOnly = true)
    public List<ApplicationResponse> getApplicationsForPost(Long jobPostId,
                                                            AuthenticatedUser currentUser) {
        verifyPostOwnership(jobPostId, currentUser.getUserId());
        return applicationRepository.findByJobPost_Id(jobPostId)
                .stream()
                .map(ApplicationResponse::from)
                .toList();
    }

    @Transactional
    public ApplicationResponse updateApplicationStatus(Long applicationId,
                                        UpdateApplicationStatusRequest request,
                                        AuthenticatedUser currentUser) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

        verifyPostOwnership(application.getJobPost().getId(), currentUser.getUserId());

        switch (request.getStatus()) {
            case ACCEPTED -> application.accept();
            case REJECTED -> application.reject();
            default -> throw new IllegalArgumentException("Invalid state transition");
        }

        //applicationRepository.save(application);
        return ApplicationResponse.from(application);
    }

    @Transactional
    public AssessmentResponse saveAssessment(Long applicationId, AssessmentRequest request,
                                             AuthenticatedUser currentUser) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

        verifyPostOwnership(application.getJobPost().getId(), currentUser.getUserId());

        ApplicationAssessment assessment = assessmentRepository
                .findByApplication_Id(applicationId)
                .map(existing -> {
                        existing.update(request.getMatchScore(), request.getEmployerNotes());
                        return existing;
                })
                .orElseGet(() -> ApplicationAssessment.create(
                        application,
                        request.getMatchScore(),
                        request.getEmployerNotes()
                ));

        assessmentRepository.save(assessment);
        return AssessmentResponse.from(assessment);
    }

    @Transactional(readOnly = true)
    public AssessmentResponse getAssessment(Long applicationId, AuthenticatedUser currentUser) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

        verifyPostOwnership(application.getJobPost().getId(), currentUser.getUserId());

        ApplicationAssessment assessment = assessmentRepository
                .findByApplication_Id(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found"));

        return AssessmentResponse.from(assessment);
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> getDocumentsForEmployer(Long applicationId,
                                                          AuthenticatedUser currentUser) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

        verifyPostOwnership(application.getJobPost().getId(), currentUser.getUserId());

        return documentRepository.findByApplication_Id(applicationId)
                .stream()
                .map(DocumentResponse::from)
                .toList();
    }

    // PRIVATE HELPERS

    private Candidate getCandidateByUserId(Long userId) {
        return candidateRepository.findByUser_Id(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found"));
    }

    private void verifyPostOwnership(Long jobPostId, Long userId) {
        JobPost jobPost = jobPostRepository.findById(jobPostId)
                .orElseThrow(() -> new ResourceNotFoundException("Job post not found"));
        Employer employer = employerRepository.findByUser_Id(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Employer profile not found"));
        if (!jobPost.getEmployer().getId().equals(employer.getId()))
            throw new AccessDeniedException("You do not have permission to access this post's applications");
    }

    private Application getApplicationOwnedByCandidate(Long applicationId, Candidate candidate) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));
        if (!application.getCandidate().getId().equals(candidate.getId()))
            throw new AccessDeniedException("You do not have permission to access this application");
        return application;
    }
}
