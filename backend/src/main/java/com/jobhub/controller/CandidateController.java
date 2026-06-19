package com.jobhub.controller;

import com.jobhub.dto.candidate.CandidateProfileResponse;
import com.jobhub.dto.candidate.UpdateCandidateProfileRequest;
import com.jobhub.security.AuthenticatedUser;
import com.jobhub.service.CandidateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/candidate/profile")
@RequiredArgsConstructor
public class CandidateController {

    private final CandidateService candidateService;

    @GetMapping
    public ResponseEntity<CandidateProfileResponse> getProfile(
            @AuthenticationPrincipal AuthenticatedUser currentUser) {
        return ResponseEntity.ok(candidateService.getProfile(currentUser));
    }

    @PutMapping
    public ResponseEntity<CandidateProfileResponse> updateProfile(
            @Valid @RequestBody UpdateCandidateProfileRequest request,
            @AuthenticationPrincipal AuthenticatedUser currentUser) {
        return ResponseEntity.ok(candidateService.updateProfile(request, currentUser));
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAccount(
            @AuthenticationPrincipal AuthenticatedUser currentUser) {
        candidateService.deleteAccount(currentUser);
        return ResponseEntity.noContent().build();
    }
}