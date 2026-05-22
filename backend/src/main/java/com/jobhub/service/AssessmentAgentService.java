package com.jobhub.service;

import com.jobhub.dto.application.AssessmentResponse;
import com.jobhub.entity.*;
import com.jobhub.enums.RecommendationType;
import com.jobhub.exception.ResourceNotFoundException;
import com.jobhub.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssessmentAgentService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationAssessmentRepository assessmentRepository;
    private final ResumeRepository resumeRepository;
    private final CandidateRepository candidateRepository;
    private final EmbeddingService embeddingService;
    private final ScoringService scoringService;
    private final ChatModel chatModel;

    @Transactional
    public AssessmentResponse generateAssessment(Long applicationId) {
        // 1. Load application
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

        JobPost jobPost = application.getJobPost();
        Candidate candidate = application.getCandidate();

        // 2. Load resume
        Resume resume = resumeRepository.findByCandidate_Id(candidate.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found"));

        // 3. Generate embeddings if missing
        if (!embeddingService.hasResumeEmbedding(resume.getId())) {
            embeddingService.generateAndStoreResumeEmbedding(resume.getId());
        }
        if (!embeddingService.hasJobPostEmbedding(jobPost.getId())) {
            embeddingService.generateAndStoreJobPostEmbedding(jobPost.getId());
        }

        // 4. Compute semantic similarity
        double semanticSimilarity = embeddingService
                .getCosineSimilarityForApplication(resume.getId(), jobPost.getId());

        log.info("Semantic similarity for application {}: {}", applicationId, semanticSimilarity);

        // 5. Run deterministic scoring
        ScoringService.ScoringResult scoringResult =
                scoringService.score(resume, jobPost, semanticSimilarity);

        // 6. Determine recommendation
        RecommendationType recommendation = determineRecommendation(scoringResult.totalScore());

        // 7. Generate LLM explanation
        String explanation = generateExplanation(
                candidate, resume, jobPost, scoringResult, recommendation);

        // 8. Upsert assessment
        ApplicationAssessment assessment = assessmentRepository
                .findByApplication_Id(applicationId)
                .map(existing -> {
                    existing.regenerate(
                            toBigDecimal(scoringResult.totalScore()),
                            toBigDecimal(scoringResult.semanticScore()),
                            toBigDecimal(scoringResult.skillsScore()),
                            toBigDecimal(scoringResult.experienceScore()),
                            toBigDecimal(scoringResult.educationScore()),
                            String.join(", ", scoringResult.matchedSkills()),
                            String.join(", ", scoringResult.missingSkills()),
                            scoringResult.experienceAssessment(),
                            scoringResult.educationAssessment(),
                            explanation,
                            recommendation
                    );
                    return existing;
                })
                .orElseGet(() -> ApplicationAssessment.create(
                        application,
                        toBigDecimal(scoringResult.totalScore()),
                        toBigDecimal(scoringResult.semanticScore()),
                        toBigDecimal(scoringResult.skillsScore()),
                        toBigDecimal(scoringResult.experienceScore()),
                        toBigDecimal(scoringResult.educationScore()),
                        String.join(", ", scoringResult.matchedSkills()),
                        String.join(", ", scoringResult.missingSkills()),
                        scoringResult.experienceAssessment(),
                        scoringResult.educationAssessment(),
                        explanation,
                        recommendation
                ));

        assessmentRepository.save(assessment);
        log.info("Assessment saved for application {} with score {}",
                applicationId, scoringResult.totalScore());

        return AssessmentResponse.from(assessment);
    }

    // ==================== PRIVATE HELPERS ====================

    private String generateExplanation(Candidate candidate, Resume resume,
                                       JobPost jobPost,
                                       ScoringService.ScoringResult scoring,
                                       RecommendationType recommendation) {
        String resumeText = embeddingService.buildResumeText(resume);
        String jobText = embeddingService.buildJobPostText(jobPost);

        String template = """
                You are an expert HR assistant analyzing candidate fit for a job position.
                Based on the structured assessment data below, write a concise professional
                explanation (3-5 sentences) of why this candidate is or isn't a good fit.
                Be specific, factual, and reference the actual data provided.
                Do not make up information not present in the data.
                                
                JOB POST:
                {jobText}
                                
                CANDIDATE RESUME:
                {resumeText}
                                
                ASSESSMENT DATA:
                - Overall match score: {totalScore}%
                - Skills score: {skillsScore}% (weight: 40%)
                - Semantic similarity score: {semanticScore}% (weight: 30%)
                - Experience score: {experienceScore}% (weight: 20%)
                - Education score: {educationScore}% (weight: 10%)
                - Matched skills: {matchedSkills}
                - Missing skills: {missingSkills}
                - Experience assessment: {experienceAssessment}
                - Education assessment: {educationAssessment}
                - Recommendation: {recommendation}
                                
                Write a professional 3-5 sentence explanation:
                """;

        PromptTemplate promptTemplate = new PromptTemplate(template);
        Prompt prompt = promptTemplate.create(Map.ofEntries(
                Map.entry("jobText", jobText),
                Map.entry("resumeText", resumeText),
                Map.entry("totalScore", String.format("%.1f", scoring.totalScore())),
                Map.entry("skillsScore", String.format("%.1f", scoring.skillsScore())),
                Map.entry("semanticScore", String.format("%.1f", scoring.semanticScore())),
                Map.entry("experienceScore", String.format("%.1f", scoring.experienceScore())),
                Map.entry("educationScore", String.format("%.1f", scoring.educationScore())),
                Map.entry("matchedSkills", scoring.matchedSkills().isEmpty()
                        ? "none detected" : String.join(", ", scoring.matchedSkills())),
                Map.entry("missingSkills", scoring.missingSkills().isEmpty()
                        ? "none" : String.join(", ", scoring.missingSkills())),
                Map.entry("experienceAssessment", scoring.experienceAssessment()),
                Map.entry("educationAssessment", scoring.educationAssessment()),
                Map.entry("recommendation", recommendation.name())
        ));

        try {
            String response = chatModel.call(prompt).getResult().getOutput().getText();
            log.info("LLM explanation generated for application");
            return response.trim();
        } catch (Exception e) {
            log.error("LLM explanation generation failed: {}", e.getMessage());
            return buildFallbackExplanation(scoring, recommendation);
        }
    }

    private String buildFallbackExplanation(ScoringService.ScoringResult scoring,
                                            RecommendationType recommendation) {
        return String.format(
                "Candidate assessment score: %.1f%%. " +
                        "Skills match: %.1f%% with %d matched and %d missing skills. " +
                        "%s Recommendation: %s.",
                scoring.totalScore(),
                scoring.skillsScore(),
                scoring.matchedSkills().size(),
                scoring.missingSkills().size(),
                scoring.experienceAssessment(),
                recommendation.name()
        );
    }

    private RecommendationType determineRecommendation(double totalScore) {
        if (totalScore >= 70.0) return RecommendationType.RECOMMENDED;
        if (totalScore >= 45.0) return RecommendationType.CONSIDER;
        return RecommendationType.NOT_RECOMMENDED;
    }

    private BigDecimal toBigDecimal(double value) {
        return BigDecimal.valueOf(Math.round(value * 100.0) / 100.0);
    }
}