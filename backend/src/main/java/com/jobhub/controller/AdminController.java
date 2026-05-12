package com.jobhub.controller;

import com.jobhub.dto.admin.AdminStatsResponse;
import com.jobhub.dto.admin.AdminUserResponse;
import com.jobhub.dto.admin.UpdateEmployerStatusRequest;
import com.jobhub.enums.UserRole;
import com.jobhub.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // USERS

    @GetMapping("/users")
    public ResponseEntity<Page<AdminUserResponse>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(adminService.getAllUsers(page, size));
    }

    @GetMapping("/users/role/{role}")
    public ResponseEntity<Page<AdminUserResponse>> getUsersByRole(
            @PathVariable UserRole role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(adminService.getUsersByRole(role, page, size));
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        adminService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/users/{userId}/deactivate")
    public ResponseEntity<Void> deactivateUser(@PathVariable Long userId) {
        adminService.deactivateUser(userId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/users/{userId}/activate")
    public ResponseEntity<Void> activateUser(@PathVariable Long userId) {
        adminService.activateUser(userId);
        return ResponseEntity.noContent().build();
    }

    // EMPLOYERS

    @GetMapping("/employers/pending")
    public ResponseEntity<Page<AdminUserResponse>> getPendingEmployers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(adminService.getPendingEmployers(page, size));
    }

    @PatchMapping("/employers/{employerId}/status")
    public ResponseEntity<AdminUserResponse> updateEmployerStatus(
            @PathVariable Long employerId,
            @Valid @RequestBody UpdateEmployerStatusRequest request) {
        return ResponseEntity.ok(adminService.updateEmployerStatus(employerId, request));
    }

    // POSTS

    /*@GetMapping("/posts")
    public ResponseEntity<Page<JobPostResponse>> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(adminService.getAllPosts(page, size));
    }

    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable Long postId) {
        adminService.deletePost(postId);
        return ResponseEntity.noContent().build();
    }*/

    // STATISTICS

    @GetMapping("/stats")
    public ResponseEntity<AdminStatsResponse> getStats() {
        return ResponseEntity.ok(adminService.getStats());
    }
}
