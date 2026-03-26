package com.jobhub.repository;

import com.jobhub.entity.JobPost;
import com.jobhub.enums.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface JobPostRepository extends JpaRepository<JobPost, Long>,
        JpaSpecificationExecutor<JobPost> {
    Page<JobPost> findByStatus(PostStatus status, Pageable pageable);
    List<JobPost> findByEmployer_IdAndStatus(Long employerId, PostStatus status);
    List<JobPost> findByEmployer_Id(Long employerId);
    long countByStatus(PostStatus status);
}
