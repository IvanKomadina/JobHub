package com.jobhub.controller;

import com.jobhub.dto.jobpost.CreateJobPostRequest;
import com.jobhub.dto.jobpost.JobPostFilterRequest;
import com.jobhub.dto.jobpost.JobPostResponse;
import com.jobhub.dto.jobpost.UpdateJobPostRequest;
import com.jobhub.security.AuthenticatedUser;
import com.jobhub.service.JobPostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class JobPostController {

    private final JobPostService jobPostService;

    // PUBLIC

    @GetMapping("/posts")
    public ResponseEntity<Page<JobPostResponse>> getPublicPosts(
            @ModelAttribute JobPostFilterRequest filter) {
        return ResponseEntity.ok(jobPostService.getPublicPosts(filter));
    }

    @GetMapping("/posts/{id}")
    public ResponseEntity<JobPostResponse> getPublicPostById(@PathVariable Long id) {
        return ResponseEntity.ok(jobPostService.getPublicPostById(id));
    }

    // EMPLOYER

    @PostMapping("/employer/posts")
    public ResponseEntity<JobPostResponse> createPost(
            @Valid @RequestBody CreateJobPostRequest request,
            @AuthenticationPrincipal AuthenticatedUser currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(jobPostService.createPost(request, currentUser));
    }

    @PutMapping("/employer/posts/{id}")
    public ResponseEntity<JobPostResponse> updatePost(
            @PathVariable Long id,
            @Valid @RequestBody UpdateJobPostRequest request,
            @AuthenticationPrincipal AuthenticatedUser currentUser) {
        return ResponseEntity.ok(jobPostService.updatePost(id, request, currentUser));
    }

    @DeleteMapping("/employer/posts/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id,
                                           @AuthenticationPrincipal AuthenticatedUser currentUser) {
        jobPostService.deletePost(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/employer/posts/{id}/close")
    public ResponseEntity<Void> closePost(@PathVariable Long id,
                                          @AuthenticationPrincipal AuthenticatedUser currentUser) {
        jobPostService.closePost(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/employer/posts")
    public ResponseEntity<List<JobPostResponse>> getMyPosts(
            @AuthenticationPrincipal AuthenticatedUser currentUser) {
        return ResponseEntity.ok(jobPostService.getMyPosts(currentUser));
    }

    // ADMIN

    @GetMapping("/admin/posts")
    public ResponseEntity<Page<JobPostResponse>> getAllPostsForAdmin(
            @ModelAttribute JobPostFilterRequest filter) {
        return ResponseEntity.ok(jobPostService.getAllPostsForAdmin(filter));
    }

    @DeleteMapping("/admin/posts/{id}")
    public ResponseEntity<Void> adminDeletePost(@PathVariable Long id) {
        jobPostService.adminDeletePost(id);
        return ResponseEntity.noContent().build();
    }
}
