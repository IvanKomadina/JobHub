package com.jobhub.controller;

import com.jobhub.dto.employer.EmployerProfileResponse;
import com.jobhub.dto.employer.UpdateEmployerProfileRequest;
import com.jobhub.security.AuthenticatedUser;
import com.jobhub.service.EmployerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/employer/profile")
@RequiredArgsConstructor
public class EmployerController {

    private final EmployerService employerService;

    @GetMapping
    public ResponseEntity<EmployerProfileResponse> getProfile(
            @AuthenticationPrincipal AuthenticatedUser currentUser) {
        return ResponseEntity.ok(employerService.getProfile(currentUser));
    }

    @PutMapping
    public ResponseEntity<EmployerProfileResponse> updateProfile(
            @Valid @RequestBody UpdateEmployerProfileRequest request,
            @AuthenticationPrincipal AuthenticatedUser currentUser) {
        return ResponseEntity.ok(employerService.updateProfile(request, currentUser));
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAccount(
            @AuthenticationPrincipal AuthenticatedUser currentUser) {
        employerService.deleteAccount(currentUser);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/logo")
    public ResponseEntity<EmployerProfileResponse> updateLogo(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal AuthenticatedUser currentUser) {
        return ResponseEntity.ok(employerService.updateLogo(file, currentUser));
    }
}