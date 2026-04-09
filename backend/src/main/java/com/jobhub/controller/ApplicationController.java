package com.jobhub.controller;

import com.jobhub.dto.application.*;
import com.jobhub.enums.DocumentType;
import com.jobhub.security.AuthenticatedUser;
import com.jobhub.service.ApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    // CANDIDATE

    /*@PostMapping("/candidate/posts/{jobPostId}/apply")
    public ResponseEntity<ApplicationResponse> apply(@PathVariable Long jobPostId,
                                                     @RequestBody CreateApplicationRequest request,
                                                     @AuthenticationPrincipal AuthenticatedUser currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(applicationService.apply(jobPostId, request, currentUser));
    }*/

    @PostMapping("/candidate/posts/{jobPostId}/draft")
    public ResponseEntity<ApplicationResponse> createDraft(
            @PathVariable Long jobPostId,
            @RequestBody CreateApplicationRequest request,
            @AuthenticationPrincipal AuthenticatedUser currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(applicationService.createDraft(jobPostId, request, currentUser));
    }

    @PostMapping("/candidate/applications/{applicationId}/submit")
    public ResponseEntity<ApplicationResponse> submitApplication(
            @PathVariable Long applicationId,
            @AuthenticationPrincipal AuthenticatedUser currentUser) {
        return ResponseEntity.ok(applicationService.submitApplication(applicationId, currentUser));
    }

    @PatchMapping("/candidate/applications/{applicationId}/withdraw")
    public ResponseEntity<Void> withdraw(@PathVariable Long applicationId,
                                         @AuthenticationPrincipal AuthenticatedUser currentUser) {
            applicationService.withdraw(applicationId, currentUser);
            return ResponseEntity.noContent().build();
    }

    @GetMapping("/candidate/applications")
    public ResponseEntity<List<ApplicationResponse>> getMyApplications(
            @AuthenticationPrincipal AuthenticatedUser currentUser) {
        return ResponseEntity.ok(applicationService.getMyApplications(currentUser));
    }

    @PostMapping(
            value = "/candidate/applications/{applicationId}/documents",
            consumes = "multipart/form-data"
    )
    public ResponseEntity<DocumentResponse> uploadDocument(
            @PathVariable Long applicationId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") DocumentType documentType,
            @AuthenticationPrincipal AuthenticatedUser currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(applicationService.uploadDocument(applicationId, file, documentType, currentUser));
    }

    @DeleteMapping("/candidate/applications/{applicationId}/documents/{documentId}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long applicationId,
                                               @PathVariable Long documentId,
                                               @AuthenticationPrincipal AuthenticatedUser currentUser) {
        applicationService.deleteDocument(applicationId, documentId, currentUser);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/candidate/applications/{applicationId}/documents")
    public ResponseEntity<List<DocumentResponse>> getDocuments(
            @PathVariable Long applicationId,
            @AuthenticationPrincipal AuthenticatedUser currentUser) {
        return ResponseEntity.ok(applicationService.getDocuments(applicationId, currentUser));
    }

    // EMPLOYER

    @GetMapping("/employer/posts/{jobPostId}/applications")
    public ResponseEntity<List<ApplicationResponse>> getApplicationsForPost(
            @PathVariable Long jobPostId,
            @AuthenticationPrincipal AuthenticatedUser currentUser) {
        return ResponseEntity.ok(applicationService.getApplicationsForPost(jobPostId, currentUser));
    }

    @PatchMapping("/employer/applications/{applicationId}/status")
    public ResponseEntity<ApplicationResponse> updateApplicationStatus(
            @PathVariable Long applicationId,
            @Valid @RequestBody UpdateApplicationStatusRequest request,
            @AuthenticationPrincipal AuthenticatedUser currentUser) {
        return ResponseEntity.ok(applicationService.updateApplicationStatus(
                applicationId, request, currentUser));
    }

    @PostMapping("/employer/applications/{applicationId}/assessment")
    public ResponseEntity<AssessmentResponse> saveAssessment(
            @PathVariable Long applicationId,
            @Valid @RequestBody AssessmentRequest request,
            @AuthenticationPrincipal AuthenticatedUser currentUser) {
        return ResponseEntity.ok(applicationService.saveAssessment(
                applicationId, request, currentUser));
    }

    @GetMapping("/employer/applications/{applicationId}/assessment")
    public ResponseEntity<AssessmentResponse> getAssessment(
            @PathVariable Long applicationId,
            @AuthenticationPrincipal AuthenticatedUser currentUser) {
        return ResponseEntity.ok(applicationService.getAssessment(applicationId, currentUser));
    }

    @GetMapping("/employer/applications/{applicationId}/documents")
    public ResponseEntity<List<DocumentResponse>> getDocumentsForEmployer(
            @PathVariable Long applicationId,
            @AuthenticationPrincipal AuthenticatedUser currentUser) {
        return ResponseEntity.ok(applicationService.getDocumentsForEmployer(applicationId, currentUser));
    }
}
