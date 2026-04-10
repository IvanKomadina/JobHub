package com.jobhub.dto.resume;

import com.jobhub.entity.*;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ResumeResponse {

    private Long id;
    private String summary;
    private LocalDateTime updatedAt;
    private List<ResumeEducationResponse> education;
    private List<ResumeExperienceResponse> experience;
    private List<ResumeSkillResponse> skills;
    private List<ResumeLanguageResponse> languages;

    public static ResumeResponse from(Resume resume,
                                      List<ResumeEducation> resumeEducation,
                                      List<ResumeExperience> resumeExperience,
                                      List<ResumeSkill> resumeSkills,
                                      List<ResumeLanguage> resumeLanguages) {
        return ResumeResponse.builder()
                .id(resume.getId())
                .summary(resume.getSummary())
                .updatedAt(resume.getUpdatedAt())
                .education(resumeEducation.stream().map(ResumeEducationResponse::from).toList())
                .experience(resumeExperience.stream().map(ResumeExperienceResponse::from).toList())
                .skills(resumeSkills.stream().map(ResumeSkillResponse::from).toList())
                .languages(resumeLanguages.stream().map(ResumeLanguageResponse::from).toList())
                .build();
    }
}
