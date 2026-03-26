package com.jobhub.repository;

import com.jobhub.entity.ApplicationAssessment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApplicationAssessmentRepository extends JpaRepository<ApplicationAssessment, Long> {
    Optional<ApplicationAssessment> findByApplication_Id(Long applicationId);
}
