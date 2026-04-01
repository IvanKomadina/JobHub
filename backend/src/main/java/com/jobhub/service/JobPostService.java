package com.jobhub.service;

import com.jobhub.dto.jobpost.CreateJobPostRequest;
import com.jobhub.dto.jobpost.JobPostFilterRequest;
import com.jobhub.dto.jobpost.JobPostResponse;
import com.jobhub.dto.jobpost.UpdateJobPostRequest;
import com.jobhub.entity.Category;
import com.jobhub.entity.Employer;
import com.jobhub.entity.JobPost;
import com.jobhub.entity.Location;
import com.jobhub.enums.EmployerStatus;
import com.jobhub.enums.PostStatus;
import com.jobhub.exception.AccessDeniedException;
import com.jobhub.exception.ResourceNotFoundException;
import com.jobhub.repository.CategoryRepository;
import com.jobhub.repository.EmployerRepository;
import com.jobhub.repository.JobPostRepository;
import com.jobhub.repository.LocationRepository;
import com.jobhub.security.AuthenticatedUser;
import com.jobhub.specification.JobPostSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JobPostService {

    private final JobPostRepository jobPostRepository;
    private final EmployerRepository employerRepository;
    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;

    // PUBLIC

    @Transactional(readOnly = true)
    public Page<JobPostResponse> getPublicPosts(JobPostFilterRequest filter) {
        Sort sort = Sort.by(
                "asc".equalsIgnoreCase(filter.getSortDirection())
                        ? Sort.Direction.ASC
                        : Sort.Direction.DESC,
                filter.getSortBy()
        );
        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);
        Specification<JobPost> spec = JobPostSpecification.withFilters(filter, false);
        return jobPostRepository.findAll(spec, pageable).map(JobPostResponse::from);
    }

    @Transactional(readOnly = true)
    public JobPostResponse getPublicPostById(Long id) {
        JobPost jobPost = jobPostRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job post not found"));

        if (jobPost.getStatus() != PostStatus.ACTIVE)
            throw new ResourceNotFoundException("Job post not found");

        return JobPostResponse.from(jobPost);
    }

    // EMPLOYER

    @Transactional
    public JobPostResponse createPost(CreateJobPostRequest request, AuthenticatedUser currentUser) {
        Employer employer = getApprovedEmployer(currentUser.getUserId());
        Category category = resolveCategory(request.getCategoryId());
        Location location = resolveLocation(request.getLocationId());

        JobPost jobPost = JobPost.create(
                employer,
                category,
                location,
                request.getTitle(),
                request.getDescription(),
                request.getRequirements(),
                request.getEmploymentType(),
                request.getSalaryMin(),
                request.getSalaryMax(),
                request.getClosesAt()
        );

        jobPostRepository.save(jobPost);
        return JobPostResponse.from(jobPost);
    }

    @Transactional
    public JobPostResponse updatePost(Long postId, UpdateJobPostRequest request,
                                      AuthenticatedUser currentUser) {
        JobPost jobPost = getPostOwnedByEmployer(postId, currentUser.getUserId());
        Category category = resolveCategory(request.getCategoryId());
        Location location = resolveLocation(request.getLocationId());

        jobPost.update(
                category,
                location,
                request.getTitle(),
                request.getDescription(),
                request.getRequirements(),
                request.getEmploymentType(),
                request.getSalaryMin(),
                request.getSalaryMax(),
                request.getClosesAt()
        );

        jobPostRepository.save(jobPost);
        return JobPostResponse.from(jobPost);
    }

    @Transactional
    public void deletePost(Long postId, AuthenticatedUser currentUser) {
        JobPost jobPost = getPostOwnedByEmployer(postId, currentUser.getUserId());
        if (jobPost.getStatus() == PostStatus.DELETED) {
            throw new IllegalStateException("Post already deleted");
        }
        jobPost.delete();
        jobPostRepository.save(jobPost);
    }

    @Transactional
    public void closePost(Long postId, AuthenticatedUser currentUser) {
        JobPost jobPost = getPostOwnedByEmployer(postId, currentUser.getUserId());
        jobPost.close();
        jobPostRepository.save(jobPost);
    }

    @Transactional(readOnly = true)
    public List<JobPostResponse> getMyPosts(AuthenticatedUser currentUser) {
        Employer employer = getEmployerByUserId(currentUser.getUserId());
        return jobPostRepository.findByEmployer_Id(employer.getId())
                .stream()
                .map(JobPostResponse::from)
                .toList();
    }

    // ADMIN

    @Transactional(readOnly = true)
    public Page<JobPostResponse> getAllPostsForAdmin(JobPostFilterRequest filter) {
        Sort sort = Sort.by(
                "asc".equalsIgnoreCase(filter.getSortDirection())
                        ? Sort.Direction.ASC
                        : Sort.Direction.DESC,
                filter.getSortBy()
        );
        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);
        Specification<JobPost> spec = JobPostSpecification.withFilters(filter, true);
        return jobPostRepository.findAll(spec, pageable).map(JobPostResponse::from);
    }

    @Transactional
    public void adminDeletePost(Long postId) {
        JobPost jobPost = jobPostRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Job post not found"));
        if (jobPost.getStatus() == PostStatus.DELETED) {
            throw new IllegalStateException("Post already deleted");
        }
        jobPost.delete();
        jobPostRepository.save(jobPost);
    }

    // PRIVATE HELPERS

    private Employer getEmployerByUserId(Long userId) {
        return employerRepository.findByUser_Id(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Employer profile not found"));
    }

    private Employer getApprovedEmployer(Long userId) {
        Employer employer = getEmployerByUserId(userId);
        if (employer.getStatus() != EmployerStatus.APPROVED)
            throw new IllegalStateException("Your account is not approved to post jobs");
        return employer;
    }

    private JobPost getPostOwnedByEmployer(Long postId, Long userId) {
        JobPost jobPost = jobPostRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Job post not found"));
        Employer employer = getEmployerByUserId(userId);

        if (!jobPost.getEmployer().getId().equals(employer.getId()))
            throw new AccessDeniedException("You do not have permission to modify this post");

        return jobPost;
    }

    private Category resolveCategory(Long categoryId) {
        if (categoryId == null)
            return null;

        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
    }

    private Location resolveLocation(Long locationId) {
        if (locationId == null)
            return null;

        return locationRepository.findById(locationId)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found"));
    }
}
