package com.jobhub.service;

import com.jobhub.entity.*;
import com.jobhub.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScoringService {

    private final ResumeSkillRepository skillRepository;
    private final ResumeExperienceRepository experienceRepository;
    private final ResumeEducationRepository educationRepository;
    private final ResumeLanguageRepository languageRepository;

    // Weights must sum to 100
    private static final double SKILLS_WEIGHT = 40.0;
    private static final double SEMANTIC_WEIGHT = 30.0;
    private static final double EXPERIENCE_WEIGHT = 20.0;
    private static final double EDUCATION_WEIGHT = 10.0;

    public ScoringResult score(Resume resume, JobPost jobPost, double semanticSimilarity) {
        SkillsResult skillsResult = scoreSkills(resume, jobPost);
        ExperienceResult experienceResult = scoreExperience(resume, jobPost);
        EducationResult educationResult = scoreEducation(resume);

        double skillsScore = skillsResult.score;
        double semanticScore = semanticSimilarity * 100;
        double experienceScore = experienceResult.score;
        double educationScore = educationResult.score;

        double totalScore =
                (skillsScore     * SKILLS_WEIGHT / 100) +
                        (semanticScore   * SEMANTIC_WEIGHT / 100) +
                        (experienceScore * EXPERIENCE_WEIGHT / 100) +
                        (educationScore  * EDUCATION_WEIGHT / 100);

        log.info("Scoring result - skills: {}, semantic: {}, experience: {}, education: {}, total: {}",
                skillsScore, semanticScore, experienceScore, educationScore, totalScore);

        return ScoringResult.builder()
                .totalScore(round(totalScore))
                .skillsScore(round(skillsScore))
                .semanticScore(round(semanticScore))
                .experienceScore(round(experienceScore))
                .educationScore(round(educationScore))
                .matchedSkills(skillsResult.matched)
                .missingSkills(skillsResult.missing)
                .experienceAssessment(experienceResult.assessment)
                .educationAssessment(educationResult.assessment)
                .build();
    }

    // ==================== SKILLS SCORING ====================

    private SkillsResult scoreSkills(Resume resume, JobPost jobPost) {
        List<ResumeSkill> candidateSkills =
                skillRepository.findByResume_IdOrderBySortOrderAsc(resume.getId());

        Set<String> candidateSkillNames = candidateSkills.stream()
                .map(ResumeSkill::getSkillName)
                .collect(Collectors.toSet());

        List<String> requiredSkills = extractSkillsFromText(
                jobPost.getRequirements() != null
                        ? jobPost.getRequirements()
                        : jobPost.getDescription()
        );

        if (requiredSkills.isEmpty()) {
            return new SkillsResult(50.0, List.of(), List.of(),
                    "No specific skills listed in job requirements");
        }

        List<String> matched = new ArrayList<>();
        List<String> missing = new ArrayList<>();

        for (String required : requiredSkills) {
            String normalizedRequired = required.trim().toLowerCase();
            boolean found = candidateSkillNames.stream()
                    .anyMatch(s -> s.contains(normalizedRequired)
                            || normalizedRequired.contains(s));
            if (found) matched.add(required);
            else missing.add(required);
        }

        double score = requiredSkills.isEmpty() ? 50.0 :
                ((double) matched.size() / requiredSkills.size()) * 100;

        return new SkillsResult(score, matched, missing, null);
    }

    // ==================== EXPERIENCE SCORING ====================

    private ExperienceResult scoreExperience(Resume resume, JobPost jobPost) {
        List<ResumeExperience> experiences =
                experienceRepository.findByResume_IdOrderBySortOrderAsc(resume.getId());

        if (experiences.isEmpty()) {
            return new ExperienceResult(0.0,
                    "No work experience listed on resume");
        }

        double totalMonths = experiences.stream()
                .mapToDouble(exp -> {
                    if (exp.getStartDate() == null) return 0;
                    java.time.LocalDate end = exp.isCurrent()
                            ? java.time.LocalDate.now()
                            : (exp.getEndDate() != null ? exp.getEndDate() : java.time.LocalDate.now());
                    return java.time.temporal.ChronoUnit.MONTHS
                            .between(exp.getStartDate(), end);
                })
                .sum();

        double years = totalMonths / 12.0;
        double score;
        String assessment;

        if (years < 1) {
            score = 25.0;
            assessment = String.format(Locale.US, "%.1f years of experience — entry level", years);
        } else if (years < 3) {
            score = 50.0;
            assessment = String.format(Locale.US, "%.1f years of experience — junior level", years);
        } else if (years < 5) {
            score = 75.0;
            assessment = String.format(Locale.US, "%.1f years of experience — mid level", years);
        } else {
            score = 100.0;
            assessment = String.format(Locale.US, "%.1f years of experience — senior level", years);
        }

        return new ExperienceResult(score, assessment);
    }

    // ==================== EDUCATION SCORING ====================

    private EducationResult scoreEducation(Resume resume) {
        List<ResumeEducation> educationList =
                educationRepository.findByResume_IdOrderBySortOrderAsc(resume.getId());

        if (educationList.isEmpty()) {
            return new EducationResult(25.0, "No education listed on resume");
        }

        double highestScore = educationList.stream()
                .mapToDouble(edu -> {
                    if (edu.getDegree() == null) return 25.0;
                    String degree = edu.getDegree().toLowerCase();
                    if (degree.contains("phd") || degree.contains("doctorate")) return 100.0;
                    if (degree.contains("master")) return 85.0;
                    if (degree.contains("bachelor") || degree.contains("bsc") ||
                            degree.contains("ba ") || degree.contains("be ")) return 70.0;
                    if (degree.contains("associate") || degree.contains("diploma")) return 50.0;
                    return 35.0;
                })
                .max()
                .orElse(25.0);

        String assessment = educationList.stream()
                .map(edu -> {
                    StringBuilder sb = new StringBuilder();
                    if (edu.getDegree() != null) sb.append(edu.getDegree());
                    if (edu.getFieldOfStudy() != null)
                        sb.append(" in ").append(edu.getFieldOfStudy());
                    sb.append(" from ").append(edu.getInstitution());
                    return sb.toString();
                })
                .findFirst()
                .orElse("Education listed");

        return new EducationResult(highestScore, assessment);
    }

    // ==================== SKILL EXTRACTION ====================

    private List<String> extractSkillsFromText(String text) {
        if (text == null || text.isBlank()) return List.of();

        // Common tech skills to look for in job post text
        List<String> knownSkills = List.of(
                "java", "spring", "spring boot", "hibernate", "jpa",
                "python", "django", "flask", "fastapi",
                "javascript", "typescript", "react", "angular", "vue",
                "node.js", "express",
                "sql", "postgresql", "mysql", "mongodb", "redis",
                "docker", "kubernetes", "aws", "azure", "gcp",
                "git", "ci/cd", "jenkins", "github actions",
                "rest", "graphql", "microservices",
                "html", "css", "tailwind",
                "kafka", "rabbitmq",
                "linux", "bash",
                "c#", ".net", "php", "ruby", "go", "rust", "kotlin", "swift"
        );

        String normalizedText = text.toLowerCase();
        return knownSkills.stream()
                .filter(normalizedText::contains)
                .collect(Collectors.toList());
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    // ==================== RESULT RECORDS ====================

    public record ScoringResult(
            double totalScore,
            double skillsScore,
            double semanticScore,
            double experienceScore,
            double educationScore,
            List<String> matchedSkills,
            List<String> missingSkills,
            String experienceAssessment,
            String educationAssessment) {

        public static ScoringResultBuilder builder() {
            return new ScoringResultBuilder();
        }

        public static class ScoringResultBuilder {
            private double totalScore;
            private double skillsScore;
            private double semanticScore;
            private double experienceScore;
            private double educationScore;
            private List<String> matchedSkills;
            private List<String> missingSkills;
            private String experienceAssessment;
            private String educationAssessment;

            public ScoringResultBuilder totalScore(double v) { this.totalScore = v; return this; }
            public ScoringResultBuilder skillsScore(double v) { this.skillsScore = v; return this; }
            public ScoringResultBuilder semanticScore(double v) { this.semanticScore = v; return this; }
            public ScoringResultBuilder experienceScore(double v) { this.experienceScore = v; return this; }
            public ScoringResultBuilder educationScore(double v) { this.educationScore = v; return this; }
            public ScoringResultBuilder matchedSkills(List<String> v) { this.matchedSkills = v; return this; }
            public ScoringResultBuilder missingSkills(List<String> v) { this.missingSkills = v; return this; }
            public ScoringResultBuilder experienceAssessment(String v) { this.experienceAssessment = v; return this; }
            public ScoringResultBuilder educationAssessment(String v) { this.educationAssessment = v; return this; }

            public ScoringResult build() {
                return new ScoringResult(totalScore, skillsScore, semanticScore,
                        experienceScore, educationScore, matchedSkills,
                        missingSkills, experienceAssessment, educationAssessment);
            }
        }
    }

    private record SkillsResult(double score, List<String> matched,
                                List<String> missing, String assessment) {}
    private record ExperienceResult(double score, String assessment) {}
    private record EducationResult(double score, String assessment) {}
}