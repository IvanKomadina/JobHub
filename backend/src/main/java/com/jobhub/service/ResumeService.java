package com.jobhub.service;

import com.jobhub.dto.resume.*;
import com.jobhub.entity.*;
import com.jobhub.exception.AccessDeniedException;
import com.jobhub.exception.ResourceNotFoundException;
import com.jobhub.repository.*;
import com.jobhub.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final ResumeEducationRepository educationRepository;
    private final ResumeExperienceRepository experienceRepository;
    private final ResumeSkillRepository skillRepository;
    private final ResumeLanguageRepository languageRepository;
    private final CandidateRepository candidateRepository;

    // RESUME

    @Transactional
    public ResumeResponse createResume(ResumeSummaryRequest request, AuthenticatedUser currentUser) {
        Candidate candidate = getCandidateByUserId(currentUser.getUserId());

        if (resumeRepository.existsByCandidate_Id(candidate.getId()))
            throw new IllegalStateException("Resume already exists");

        Resume resume = Resume.create(candidate, request.getSummary());
        resumeRepository.save(resume);
        return buildResumeResponse(resume);
    }

    @Transactional(readOnly = true)
    public ResumeResponse getResume(AuthenticatedUser currentUser) {
        Resume resume = getResumeByUserId(currentUser.getUserId());
        return buildResumeResponse(resume);
    }

    @Transactional
    public ResumeResponse updateSummary(ResumeSummaryRequest request, AuthenticatedUser currentUser) {
        Resume resume = getResumeByUserId(currentUser.getUserId());
        resume.updateSummary(request.getSummary());
        return buildResumeResponse(resume);
    }

    // EDUCATION

    @Transactional
    public ResumeEducationResponse addEducation(ResumeEducationRequest request,
                                                AuthenticatedUser currentUser) {
        Resume resume = getResumeByUserId(currentUser.getUserId());
        ResumeEducation education = ResumeEducation.create(
                resume,
                request.getInstitution(),
                request.getDegree(),
                request.getFieldOfStudy(),
                request.getStartDate(),
                request.getEndDate(),
                request.getDescription(),
                request.getSortOrder()
        );
        educationRepository.save(education);
        return ResumeEducationResponse.from(education);
    }

    @Transactional
    public ResumeEducationResponse updateEducation(Long educationId,
                                                   ResumeEducationRequest request,
                                                   AuthenticatedUser currentUser) {
        ResumeEducation education = getEducationOwnedByUser(educationId, currentUser.getUserId());
        education.update(
                request.getInstitution(),
                request.getDegree(),
                request.getFieldOfStudy(),
                request.getStartDate(),
                request.getEndDate(),
                request.getDescription(),
                request.getSortOrder()
        );
        return ResumeEducationResponse.from(education);
    }

    @Transactional
    public void deleteEducation(Long educationId, AuthenticatedUser currentUser) {
        ResumeEducation education = getEducationOwnedByUser(educationId, currentUser.getUserId());
        educationRepository.delete(education);
    }

    // EXPERIENCE

    @Transactional
    public ResumeExperienceResponse addExperience(ResumeExperienceRequest request,
                                                  AuthenticatedUser currentUser) {
        Resume resume = getResumeByUserId(currentUser.getUserId());
        ResumeExperience experience = ResumeExperience.create(
                resume,
                request.getCompany(),
                request.getPosition(),
                request.getLocation(),
                request.getStartDate(),
                request.getEndDate(),
                request.getDescription(),
                request.getSortOrder()
        );
        experienceRepository.save(experience);
        return ResumeExperienceResponse.from(experience);
    }

    @Transactional
    public ResumeExperienceResponse updateExperience(Long experienceId,
                                                     ResumeExperienceRequest request,
                                                     AuthenticatedUser currentUser) {
        ResumeExperience experience = getExperienceOwnedByUser(experienceId, currentUser.getUserId());
        experience.update(
                request.getCompany(),
                request.getPosition(),
                request.getLocation(),
                request.getStartDate(),
                request.getEndDate(),
                request.getDescription(),
                request.getSortOrder()
        );
        return ResumeExperienceResponse.from(experience);
    }

    @Transactional
    public void deleteExperience(Long experienceId, AuthenticatedUser currentUser) {
        ResumeExperience experience = getExperienceOwnedByUser(experienceId, currentUser.getUserId());
        experienceRepository.delete(experience);
    }

    // SKILLS

    @Transactional
    public ResumeSkillResponse addSkill(ResumeSkillRequest request, AuthenticatedUser currentUser) {
        Resume resume = getResumeByUserId(currentUser.getUserId());
        String normalized = request.getSkillName().trim().toLowerCase();
        if (skillRepository.existsByResume_IdAndSkillName(resume.getId(), normalized))
            throw new IllegalStateException("Skill already exists on your resume");

        ResumeSkill skill = ResumeSkill.create(
                resume,
                request.getSkillName(),
                request.getSkillLevel(),
                request.getSortOrder()
        );
        skillRepository.save(skill);
        return ResumeSkillResponse.from(skill);
    }

    @Transactional
    public ResumeSkillResponse updateSkill(Long skillId, ResumeSkillRequest request,
                                           AuthenticatedUser currentUser) {
        ResumeSkill skill = getSkillOwnedByUser(skillId, currentUser.getUserId());
        skill.update(request.getSkillName(), request.getSkillLevel(), request.getSortOrder());
        return ResumeSkillResponse.from(skill);
    }

    @Transactional
    public void deleteSkill(Long skillId, AuthenticatedUser currentUser) {
        ResumeSkill skill = getSkillOwnedByUser(skillId, currentUser.getUserId());
        skillRepository.delete(skill);
    }

    // LANGUAGES

    @Transactional
    public ResumeLanguageResponse addLanguage(ResumeLanguageRequest request,
                                              AuthenticatedUser currentUser) {
        Resume resume = getResumeByUserId(currentUser.getUserId());
        String normalized = request.getLanguageName().trim().toLowerCase();
        if (languageRepository.existsByResume_IdAndLanguageName(resume.getId(), normalized))
            throw new IllegalStateException("Language already exists on your resume");

        ResumeLanguage language = ResumeLanguage.create(
                resume,
                request.getLanguageName(),
                request.getLanguageLevel(),
                request.getSortOrder()
        );
        languageRepository.save(language);
        return ResumeLanguageResponse.from(language);
    }

    @Transactional
    public ResumeLanguageResponse updateLanguage(Long languageId, ResumeLanguageRequest request,
                                                 AuthenticatedUser currentUser) {
        ResumeLanguage language = getLanguageOwnedByUser(languageId, currentUser.getUserId());
        language.update(request.getLanguageName(), request.getLanguageLevel(), request.getSortOrder());
        return ResumeLanguageResponse.from(language);
    }

    @Transactional
    public void deleteLanguage(Long languageId, AuthenticatedUser currentUser) {
        ResumeLanguage language = getLanguageOwnedByUser(languageId, currentUser.getUserId());
        languageRepository.delete(language);
    }

    // PRIVATE HELPERS

    private Candidate getCandidateByUserId(Long userId) {
        return candidateRepository.findByUser_Id(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found"));
    }

    private Resume getResumeByUserId(Long userId) {
        Candidate candidate = getCandidateByUserId(userId);
        return resumeRepository.findByCandidate_Id(candidate.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found"));
    }

    private ResumeResponse buildResumeResponse(Resume resume) {
        return ResumeResponse.from(
                resume,
                educationRepository.findByResume_IdOrderBySortOrderAsc(resume.getId()),
                experienceRepository.findByResume_IdOrderBySortOrderAsc(resume.getId()),
                skillRepository.findByResume_IdOrderBySortOrderAsc(resume.getId()),
                languageRepository.findByResume_IdOrderBySortOrderAsc(resume.getId())
        );
    }

    private ResumeEducation getEducationOwnedByUser(Long educationId, Long userId) {
        ResumeEducation education = educationRepository.findById(educationId)
                .orElseThrow(() -> new ResourceNotFoundException("Education entry not found"));
        verifyResumeOwnership(education.getResume(), userId);
        return education;
    }

    private ResumeExperience getExperienceOwnedByUser(Long experienceId, Long userId) {
        ResumeExperience experience = experienceRepository.findById(experienceId)
                .orElseThrow(() -> new ResourceNotFoundException("Experience entry not found"));
        verifyResumeOwnership(experience.getResume(), userId);
        return experience;
    }

    private ResumeSkill getSkillOwnedByUser(Long skillId, Long userId) {
        ResumeSkill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found"));
        verifyResumeOwnership(skill.getResume(), userId);
        return skill;
    }

    private ResumeLanguage getLanguageOwnedByUser(Long languageId, Long userId) {
        ResumeLanguage language = languageRepository.findById(languageId)
                .orElseThrow(() -> new ResourceNotFoundException("Language not found"));
        verifyResumeOwnership(language.getResume(), userId);
        return language;
    }

    private void verifyResumeOwnership(Resume resume, Long userId) {
        Candidate candidate = getCandidateByUserId(userId);
        if (!resume.getCandidate().getId().equals(candidate.getId()))
            throw new AccessDeniedException("You do not have permission to modify this resume");
    }
}