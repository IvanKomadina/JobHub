package com.jobhub.repository;

import com.jobhub.entity.JobPost;
import com.jobhub.enums.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface JobPostRepository extends JpaRepository<JobPost, Long>,
        JpaSpecificationExecutor<JobPost> {
    Page<JobPost> findByStatus(PostStatus status, Pageable pageable);
    List<JobPost> findByEmployer_IdAndStatus(Long employerId, PostStatus status);
    List<JobPost> findByEmployer_Id(Long employerId);
    long countByStatus(PostStatus status);

    @Modifying
    @Transactional
    @Query(value = "UPDATE job_posts SET embedding = CAST(:embedding AS vector) WHERE id = :id", nativeQuery = true)
    void updateEmbedding(@Param("id") Long id, @Param("embedding") String embedding);

    @Query(value = "SELECT embedding::text FROM job_posts WHERE id = :id", nativeQuery = true)
    String findEmbeddingById(@Param("id") Long id);
}
