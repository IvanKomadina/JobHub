package com.jobhub.controller;

import com.jobhub.dto.application.AssessmentResponse;
import com.jobhub.dto.application.UpdateAssessmentNotesRequest;
import com.jobhub.security.AuthenticatedUser;
import com.jobhub.service.AssessmentAgentService;
import com.jobhub.service.AssessmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employer/applications/{applicationId}/assessment")
@RequiredArgsConstructor
public class AssessmentController {

    private final AssessmentAgentService assessmentAgentService;
    private final AssessmentService assessmentService;

    @PostMapping("/generate")
    public ResponseEntity<AssessmentResponse> generateAssessment(
            @PathVariable Long applicationId,
            @AuthenticationPrincipal AuthenticatedUser currentUser) {
        return ResponseEntity.ok(
                assessmentAgentService.generateAssessment(applicationId));
    }

    @GetMapping
    public ResponseEntity<AssessmentResponse> getAssessment(
            @PathVariable Long applicationId,
            @AuthenticationPrincipal AuthenticatedUser currentUser) {
        return ResponseEntity.ok(
                assessmentService.getAssessment(applicationId, currentUser));
    }

    @PatchMapping("/notes")
    public ResponseEntity<AssessmentResponse> updateNotes(
            @PathVariable Long applicationId,
            @Valid @RequestBody UpdateAssessmentNotesRequest request,
            @AuthenticationPrincipal AuthenticatedUser currentUser) {
        return ResponseEntity.ok(
                assessmentService.updateNotes(applicationId, request, currentUser));
    }
}