package com.jobhub.repository;

import com.jobhub.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ResumeRepository extends JpaRepository<Resume, Long> {
    Optional<Resume> findByCandidate_Id(Long candidateId);
    boolean existsByCandidate_Id(Long candidateId);
}
