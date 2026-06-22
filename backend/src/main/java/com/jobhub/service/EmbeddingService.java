package com.jobhub.service;

import com.jobhub.entity.JobPost;
import com.jobhub.entity.Resume;
import com.jobhub.entity.ResumeEducation;
import com.jobhub.entity.ResumeExperience;
import com.jobhub.entity.ResumeSkill;
import com.jobhub.entity.ResumeLanguage;
import com.jobhub.exception.ResourceNotFoundException;
import com.jobhub.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmbeddingService {

    private final EmbeddingModel embeddingModel;
    private final ResumeRepository resumeRepository;
    private final JobPostRepository jobPostRepository;
    private final ResumeEducationRepository educationRepository;
    private final ResumeExperienceRepository experienceRepository;
    private final ResumeSkillRepository skillRepository;
    private final ResumeLanguageRepository languageRepository;

    // ==================== RESUME EMBEDDING ====================

    @Transactional
    public void generateAndStoreResumeEmbedding(Long resumeId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found"));

        String resumeText = buildResumeText(resume);
        float[] embedding = generateEmbedding(resumeText);

        resumeRepository.updateEmbedding(resumeId, floatArrayToString(embedding));
        log.info("Generated embedding for resume {}", resumeId);
    }

    @Async("taskExecutor")
    @Transactional
    public void generateAndStoreJobPostEmbeddingAsync(Long jobPostId) {
        try {
            generateAndStoreJobPostEmbedding(jobPostId);
            log.info("Async embedding generated for job post {}", jobPostId);
        } catch (Exception e) {
            log.error("Failed to generate embedding for job post {}: {}",
                    jobPostId, e.getMessage());
        }
    }

    // ==================== JOB POST EMBEDDING ====================

    @Transactional
    public void generateAndStoreJobPostEmbedding(Long jobPostId) {
        JobPost jobPost = jobPostRepository.findById(jobPostId)
                .orElseThrow(() -> new RuntimeException("Job post not found"));

        String jobPostText = buildJobPostText(jobPost);
        float[] embedding = generateEmbedding(jobPostText);

        jobPostRepository.updateEmbedding(jobPostId, floatArrayToString(embedding));
        log.info("Generated embedding for job post {}", jobPostId);
    }

    @Async("taskExecutor")
    @Transactional
    public void generateAndStoreResumeEmbeddingAsync(Long resumeId) {
        try {
            generateAndStoreResumeEmbedding(resumeId);
            log.info("Async embedding generated for resume {}", resumeId);
        } catch (Exception e) {
            log.error("Failed to generate embedding for resume {}: {}",
                    resumeId, e.getMessage());
        }
    }

    // ==================== SIMILARITY ====================

    public double computeCosineSimilarity(float[] vectorA, float[] vectorB) {
        if (vectorA == null || vectorB == null) return 0.0;
        if (vectorA.length != vectorB.length)
            throw new IllegalArgumentException("Vectors must have the same dimension");

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += vectorA[i] * vectorA[i];
            normB += vectorB[i] * vectorB[i];
        }

        if (normA == 0.0 || normB == 0.0) return 0.0;
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    public double getCosineSimilarityForApplication(Long resumeId, Long jobPostId) {
        String resumeEmbeddingStr = resumeRepository.findEmbeddingById(resumeId);
        String jobPostEmbeddingStr = jobPostRepository.findEmbeddingById(jobPostId);

        if (resumeEmbeddingStr == null || jobPostEmbeddingStr == null) return 0.0;

        float[] resumeEmbedding = stringToFloatArray(resumeEmbeddingStr);
        float[] jobPostEmbedding = stringToFloatArray(jobPostEmbeddingStr);

        return computeCosineSimilarity(resumeEmbedding, jobPostEmbedding);
    }

    public boolean hasResumeEmbedding(Long resumeId) {
        String embedding = resumeRepository.findEmbeddingById(resumeId);
        return embedding != null && !embedding.isBlank();
    }

    public boolean hasJobPostEmbedding(Long jobPostId) {
        String embedding = jobPostRepository.findEmbeddingById(jobPostId);
        return embedding != null && !embedding.isBlank();
    }

    // ==================== TEXT BUILDERS ====================

    public String buildResumeText(Resume resume) {
        List<ResumeEducation> education =
                educationRepository.findByResume_IdOrderBySortOrderAsc(resume.getId());
        List<ResumeExperience> experience =
                experienceRepository.findByResume_IdOrderBySortOrderAsc(resume.getId());
        List<ResumeSkill> skills =
                skillRepository.findByResume_IdOrderBySortOrderAsc(resume.getId());
        List<ResumeLanguage> languages =
                languageRepository.findByResume_IdOrderBySortOrderAsc(resume.getId());

        StringBuilder sb = new StringBuilder();

        if (resume.getSummary() != null && !resume.getSummary().isBlank()) {
            sb.append("SUMMARY:\n").append(resume.getSummary()).append("\n\n");
        }

        if (!experience.isEmpty()) {
            sb.append("WORK EXPERIENCE:\n");
            experience.forEach(exp -> {
                sb.append(exp.getPosition()).append(" at ").append(exp.getCompany());
                if (exp.getDescription() != null)
                    sb.append(": ").append(exp.getDescription());
                sb.append("\n");
            });
            sb.append("\n");
        }

        if (!education.isEmpty()) {
            sb.append("EDUCATION:\n");
            education.forEach(edu -> {
                sb.append(edu.getInstitution());
                if (edu.getDegree() != null)
                    sb.append(", ").append(edu.getDegree());
                if (edu.getFieldOfStudy() != null)
                    sb.append(" in ").append(edu.getFieldOfStudy());
                sb.append("\n");
            });
            sb.append("\n");
        }

        if (!skills.isEmpty()) {
            sb.append("SKILLS:\n");
            sb.append(skills.stream()
                    .map(s -> s.getDisplayName() +
                            (s.getSkillLevel() != null ? " (" + s.getSkillLevel() + ")" : ""))
                    .collect(Collectors.joining(", ")));
            sb.append("\n\n");
        }

        if (!languages.isEmpty()) {
            sb.append("LANGUAGES:\n");
            List<String> languageStrings = languages.stream()
                    .map(l -> l.getLanguageName() +
                            (l.getLanguageLevel() != null ? " (" + l.getLanguageLevel() + ")" : ""))
                    .toList();
            sb.append(String.join(", ", languageStrings));
            sb.append("\n");
        }

        return sb.toString();
    }

    public String buildJobPostText(JobPost jobPost) {
        StringBuilder sb = new StringBuilder();

        sb.append("JOB TITLE:\n").append(jobPost.getTitle()).append("\n\n");
        sb.append("DESCRIPTION:\n").append(jobPost.getDescription()).append("\n\n");

        if (jobPost.getRequirements() != null && !jobPost.getRequirements().isBlank()) {
            sb.append("REQUIREMENTS:\n").append(jobPost.getRequirements()).append("\n\n");
        }

        if (jobPost.getEmploymentType() != null) {
            sb.append("EMPLOYMENT TYPE:\n")
                    .append(jobPost.getEmploymentType().name()).append("\n\n");
        }

        if (jobPost.getCategory() != null) {
            sb.append("CATEGORY:\n")
                    .append(jobPost.getCategory().getName()).append("\n");
        }

        return sb.toString();
    }

    // ==================== PRIVATE HELPERS ====================

    public float[] generateEmbedding(String text) {
        float[] embedding = embeddingModel.embed(text);
        log.debug("Generated embedding of dimension {}", embedding.length);
        return embedding;
    }

    private String floatArrayToString(float[] array) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < array.length; i++) {
            sb.append(array[i]);
            if (i < array.length - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    private float[] stringToFloatArray(String str) {
        str = str.trim().replaceAll("[\\[\\]]", "");
        String[] parts = str.split(",");
        float[] result = new float[parts.length];
        for (int i = 0; i < parts.length; i++) {
            result[i] = Float.parseFloat(parts[i].trim());
        }
        return result;
    }
}