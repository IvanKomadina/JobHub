package com.jobhub.repository;

import com.jobhub.entity.ResumeExperience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResumeExperienceRepository extends JpaRepository<ResumeExperience, Long> {
    List<ResumeExperience> findByResume_IdOrderBySortOrderAsc(Long resumeId);
}
