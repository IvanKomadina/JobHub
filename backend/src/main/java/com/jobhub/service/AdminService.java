package com.jobhub.service;

import com.jobhub.dto.admin.AdminStatsResponse;
import com.jobhub.dto.admin.AdminUserResponse;
import com.jobhub.dto.admin.UpdateEmployerStatusRequest;
import com.jobhub.entity.Employer;
import com.jobhub.entity.User;
import com.jobhub.enums.EmployerStatus;
import com.jobhub.enums.PostStatus;
import com.jobhub.enums.UserRole;
import com.jobhub.exception.ResourceNotFoundException;
import com.jobhub.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final EmployerRepository employerRepository;
    private final JobPostRepository jobPostRepository;
    private final ApplicationRepository applicationRepository;
    private final CandidateRepository candidateRepository;

    // USERS

    @Transactional(readOnly = true)
    public Page<AdminUserResponse> getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return userRepository.findAll(pageable).map(AdminUserResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<AdminUserResponse> getUsersByRole(UserRole role, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return userRepository.findByRole(role, pageable).map(AdminUserResponse::from);
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (user.getRole() == UserRole.ADMINISTRATOR)
            throw new IllegalStateException("Cannot delete administrator account");

        if (user.getRole() == UserRole.CANDIDATE) {
            candidateRepository.findByUser_Id(userId)
                    .ifPresent(candidateRepository::delete);
        } else if (user.getRole() == UserRole.EMPLOYER) {
            employerRepository.findByUser_Id(userId)
                    .ifPresent(employerRepository::delete);
        }
        userRepository.delete(user);
    }

    @Transactional
    public void deactivateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (user.getRole() == UserRole.ADMINISTRATOR)
            throw new IllegalStateException("Cannot deactivate administrator account");
        user.deactivate();
    }

    @Transactional
    public void activateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.activate();
    }

    // EMPLOYERS

    @Transactional(readOnly = true)
    public Page<AdminUserResponse> getPendingEmployers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return userRepository.findByRoleAndEmployerStatus(
                UserRole.EMPLOYER, EmployerStatus.PENDING, pageable)
                .map(AdminUserResponse::from);
    }

    @Transactional
    public AdminUserResponse updateEmployerStatus(Long employerId, UpdateEmployerStatusRequest request) {
        Employer employer = employerRepository.findById(employerId)
                .orElseThrow(() -> new ResourceNotFoundException("Employer profile not found"));

        if (request.getStatus() == EmployerStatus.APPROVED)
            employer.approve();
        else if (request.getStatus() == EmployerStatus.REJECTED)
            employer.reject();
        else
            throw new IllegalArgumentException("Invalid status transition");

        employerRepository.save(employer);
        return AdminUserResponse.from(employer.getUser());
    }

    // POSTS

    /*@Transactional(readOnly = true)
    public Page<JobPostResponse> getAllPosts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return jobPostRepository.findAll(pageable).map(JobPostResponse::from);
    }

    @Transactional
    public void deletePost(Long postId) {
        JobPost post = jobPostRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Job post not found"));
        post.delete();
        jobPostRepository.save(post);
    }*/

    // STATISTICS

    @Transactional(readOnly = true)
    public AdminStatsResponse getStats() {
        return AdminStatsResponse.builder()
                .totalCandidates(userRepository.countByRole(UserRole.CANDIDATE))
                .totalEmployers(userRepository.countByRole(UserRole.EMPLOYER))
                .activePosts(jobPostRepository.countByStatus(PostStatus.ACTIVE))
                .totalPosts(jobPostRepository.count())
                .totalApplications(applicationRepository.count())
                .build();
    }
}
