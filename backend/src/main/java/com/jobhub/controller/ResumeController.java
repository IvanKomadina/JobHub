package com.jobhub.controller;

import com.jobhub.dto.resume.*;
import com.jobhub.entity.ResumeExperience;
import com.jobhub.security.AuthenticatedUser;
import com.jobhub.service.ResumeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/candidate/resume")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;

    // RESUME

    @PostMapping
    public ResponseEntity<ResumeResponse> createResume(
            @RequestBody ResumeSummaryRequest request,
            @AuthenticationPrincipal AuthenticatedUser currentUser
            ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(resumeService.createResume(request, currentUser));
    }

    @GetMapping
    public ResponseEntity<ResumeResponse> getResume(@AuthenticationPrincipal AuthenticatedUser currentUser) {
        return ResponseEntity.ok(resumeService.getResume(currentUser));
    }

    @PatchMapping("/summary")
    public ResponseEntity<ResumeResponse> updateSummary(
            @RequestBody ResumeSummaryRequest request,
            @AuthenticationPrincipal AuthenticatedUser currentUser) {
        return ResponseEntity.ok(resumeService.updateSummary(request, currentUser));
    }

    // EDUCATION

    @PostMapping("/education")
    public ResponseEntity<ResumeEducationResponse> addEducation(
            @Valid @RequestBody ResumeEducationRequest request,
            @AuthenticationPrincipal AuthenticatedUser currentUser
            ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(resumeService.addEducation(request, currentUser));
    }

    @PutMapping("/education/{id}")
    public ResponseEntity<ResumeEducationResponse> updateEducation(
            @PathVariable Long id,
            @Valid @RequestBody ResumeEducationRequest request,
            @AuthenticationPrincipal AuthenticatedUser currentUser) {
        return ResponseEntity.ok(resumeService.updateEducation(id, request, currentUser));
    }

    @DeleteMapping("/education/{id}")
    public ResponseEntity<Void> deleteEducation(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser currentUser) {
        resumeService.deleteEducation(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    // EXPERIENCE

    @PostMapping("/experience")
    public ResponseEntity<ResumeExperienceResponse> addExperience(
            @Valid @RequestBody ResumeExperienceRequest request,
            @AuthenticationPrincipal AuthenticatedUser currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(resumeService.addExperience(request, currentUser));
    }

    @PutMapping("/experience/{id}")
    public ResponseEntity<ResumeExperienceResponse> updateExperience(
            @PathVariable Long id,
            @Valid @RequestBody ResumeExperienceRequest request,
            @AuthenticationPrincipal AuthenticatedUser currentUser) {
        return ResponseEntity.ok(resumeService.updateExperience(id, request, currentUser));
    }

    @DeleteMapping("/experience/{id}")
    public ResponseEntity<Void> deleteExperience(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser currentUser) {
        resumeService.deleteExperience(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    // SKILLS

    @PostMapping("/skills")
    public ResponseEntity<ResumeSkillResponse> addSkill(
            @Valid @RequestBody ResumeSkillRequest request,
            @AuthenticationPrincipal AuthenticatedUser currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(resumeService.addSkill(request, currentUser));
    }

    @PutMapping("/skills/{id}")
    public ResponseEntity<ResumeSkillResponse> updateSkill(
            @PathVariable Long id,
            @Valid @RequestBody ResumeSkillRequest request,
            @AuthenticationPrincipal AuthenticatedUser currentUser) {
        return ResponseEntity.ok(resumeService.updateSkill(id, request, currentUser));
    }

    @DeleteMapping("/skills/{id}")
    public ResponseEntity<Void> deleteSkill(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser currentUser) {
        resumeService.deleteSkill(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    // LANGUAGES

    @PostMapping("/languages")
    public ResponseEntity<ResumeLanguageResponse> addLanguage(
            @Valid @RequestBody ResumeLanguageRequest request,
            @AuthenticationPrincipal AuthenticatedUser currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(resumeService.addLanguage(request, currentUser));
    }

    @PutMapping("/languages/{id}")
    public ResponseEntity<ResumeLanguageResponse> updateLanguage(
            @PathVariable Long id,
            @Valid @RequestBody ResumeLanguageRequest request,
            @AuthenticationPrincipal AuthenticatedUser currentUser) {
        return ResponseEntity.ok(resumeService.updateLanguage(id, request, currentUser));
    }

    @DeleteMapping("/languages/{id}")
    public ResponseEntity<Void> deleteLanguage(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser currentUser) {
        resumeService.deleteLanguage(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}
