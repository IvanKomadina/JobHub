package com.jobhub.repository;

import com.jobhub.entity.ResumeEducation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResumeEducationRepository extends JpaRepository<ResumeEducation, Long> {
    List<ResumeEducation> findByResume_IdOrderBySortOrderAsc(Long resumeId);
}
