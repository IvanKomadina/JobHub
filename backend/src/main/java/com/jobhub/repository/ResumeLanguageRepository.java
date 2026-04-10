package com.jobhub.repository;

import com.jobhub.entity.ResumeLanguage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResumeLanguageRepository extends JpaRepository<ResumeLanguage, Long> {
    List<ResumeLanguage> findByResume_IdOrderBySortOrderAsc(Long resumeId);
    boolean existsByResume_IdAndLanguageName(Long resumeId, String languageName);
}
