package com.jobhub.repository;

import com.jobhub.entity.ResumeSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResumeSkillRepository extends JpaRepository<ResumeSkill, Long> {
    List<ResumeSkill> findByResume_IdOrderBySortOrderAsc(Long resumeId);
    boolean existsByResume_IdAndSkillName(Long resumeId, String skillName);
}
