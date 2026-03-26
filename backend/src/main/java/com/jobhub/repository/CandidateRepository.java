package com.jobhub.repository;

import com.jobhub.entity.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CandidateRepository extends JpaRepository<Candidate, Long> {
    Optional<Candidate> findByUser_Id(Long userId);
    boolean existsByUser_Id(Long userId);
}
