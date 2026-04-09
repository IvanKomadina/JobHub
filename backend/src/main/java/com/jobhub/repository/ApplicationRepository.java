package com.jobhub.repository;

import com.jobhub.entity.Application;
import com.jobhub.enums.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    List<Application> findByCandidate_Id(Long candidateId);
    List<Application> findByJobPost_Id(Long jobPostId);
    Optional<Application> findByJobPost_IdAndCandidate_Id(Long jobPostId, Long candidateId);
    boolean existsByJobPost_IdAndCandidate_IdAndStatusNot(Long jobPostId, Long candidateId, ApplicationStatus status);
    long countByJobPost_Id(Long jobPostId);
    List<Application> findByJobPost_IdAndStatus(Long jobPostId, ApplicationStatus status);
}
