package com.jobhub.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobhub.entity.*;
import com.jobhub.enums.LanguageLevel;
import com.jobhub.enums.SkillLevel;
import com.jobhub.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.Loader;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CvParsingService {

    private final ChatModel chatModel;
    private final ResumeRepository resumeRepository;
    private final ResumeEducationRepository educationRepository;
    private final ResumeExperienceRepository experienceRepository;
    private final ResumeSkillRepository skillRepository;
    private final ResumeLanguageRepository languageRepository;
    private final CandidateRepository candidateRepository;
    private final ObjectMapper objectMapper;

    private static final String SYSTEM_PROMPT = """
            You are a CV parser. Extract structured information from CV text.
            Return ONLY a valid JSON object with no preamble, explanation, or markdown.
            Use null for missing fields. Dates must be in YYYY-MM format.
            For skills, extract only the skill name as a simple string.
            Languages refer ONLY to human spoken languages, not programming languages.
            Programming languages such as Java, Python, JavaScript, C++, etc. must be included in skills, not languages.
            For language levels use only: A1, A2, B1, B2, C1, C2, NATIVE.
            If language level is not specified use null.
            """;

    private static final String USER_PROMPT_TEMPLATE = """
            Extract information from this CV and return ONLY this JSON structure:
            {
              "summary": "string or null",
              "experience": [
                {
                  "company": "string",
                  "position": "string",
                  "location": "string or null",
                  "startDate": "YYYY-MM or null",
                  "endDate": "YYYY-MM or null",
                  "current": boolean,
                  "description": "string or null"
                }
              ],
              "education": [
                {
                  "institution": "string",
                  "degree": "string or null",
                  "fieldOfStudy": "string or null",
                  "startDate": "YYYY-MM or null",
                  "endDate": "YYYY-MM or null",
                  "description": "string or null"
                }
              ],
              "skills": ["skill1", "skill2"],
              "languages": [
                {
                  "language": "string",
                  "level": "A1|A2|B1|B2|C1|C2|NATIVE or null"
                }
              ]
            }
                        
            CV TEXT:
            %s
            """;

    @Transactional
    public void parseAndStoreResume(MultipartFile file, Candidate candidate) {
        log.info("Starting CV parsing for candidate {}", candidate.getId());

        // 1. Extract text from PDF
        String cvText = extractTextFromPdf(file);
        if (cvText == null || cvText.isBlank()) {
            log.warn("Could not extract text from CV for candidate {}", candidate.getId());
            return;
        }

        // 2. Parse with LLM
        String jsonResponse = callLlmForParsing(cvText);
        if (jsonResponse == null) {
            log.warn("LLM parsing failed for candidate {}", candidate.getId());
            return;
        }

        // 3. Get or create resume
        Resume resume = resumeRepository.findByCandidate_Id(candidate.getId())
                .orElseGet(() -> resumeRepository.save(Resume.create(candidate, null)));

        // 4. Clear existing data
        clearExistingResumeData(resume.getId());

        // 5. Store parsed data
        storeParsedData(resume, jsonResponse);

        log.info("CV parsing completed for candidate {}", candidate.getId());
    }

    // ==================== PRIVATE HELPERS ====================

    private String extractTextFromPdf(MultipartFile file) {
        try {
            PDDocument document = Loader.loadPDF(file.getBytes());
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            document.close();
            log.debug("Extracted {} characters from PDF", text.length());
            return text;
        } catch (IOException e) {
            log.error("Failed to extract text from PDF: {}", e.getMessage());
            return null;
        }
    }

    private String callLlmForParsing(String cvText) {
        try {
            String truncatedText = cvText.length() > 6000
                    ? cvText.substring(0, 6000) : cvText;

            Prompt prompt = new Prompt(List.of(
                    new SystemMessage(SYSTEM_PROMPT),
                    new UserMessage(String.format(USER_PROMPT_TEMPLATE, truncatedText))
            ));

            String response = chatModel.call(prompt)
                    .getResult()
                    .getOutput()
                    .getText();

            // Remove thinking tokens if qwen3 outputs them
            if (response.contains("<think>")) {
                response = response.replaceAll("<think>[\\s\\S]*?</think>", "").trim();
            }

            // Extract JSON if wrapped in markdown
            if (response.contains("```json")) {
                response = response.substring(
                        response.indexOf("```json") + 7,
                        response.lastIndexOf("```")
                ).trim();
            } else if (response.contains("```")) {
                response = response.substring(
                        response.indexOf("```") + 3,
                        response.lastIndexOf("```")
                ).trim();
            }

            log.debug("LLM parsing response: {}", response);
            return response;

        } catch (Exception e) {
            log.error("LLM parsing call failed: {}", e.getMessage());
            return null;
        }
    }

    private void clearExistingResumeData(Long resumeId) {
        educationRepository.deleteByResume_Id(resumeId);
        experienceRepository.deleteByResume_Id(resumeId);
        skillRepository.deleteByResume_Id(resumeId);
        languageRepository.deleteByResume_Id(resumeId);
        log.debug("Cleared existing resume data for resume {}", resumeId);
    }

    private void storeParsedData(Resume resume, String jsonResponse) {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);

            // Summary
            if (root.has("summary") && !root.get("summary").isNull()) {
                resume.updateSummary(root.get("summary").asText());
                resumeRepository.save(resume);
            }

            // Experience
            JsonNode experience = root.get("experience");
            if (experience != null && experience.isArray()) {
                int sortOrder = 0;
                for (JsonNode exp : experience) {
                    try {
                        ResumeExperience entry = ResumeExperience.create(
                                resume,
                                getTextOrNull(exp, "company"),
                                getTextOrNull(exp, "position"),
                                getTextOrNull(exp, "location"),
                                parseDate(getTextOrNull(exp, "startDate")),
                                exp.has("current") && exp.get("current").asBoolean()
                                        ? null
                                        : parseDate(getTextOrNull(exp, "endDate")),
                                getTextOrNull(exp, "description"),
                                sortOrder++
                        );
                        experienceRepository.save(entry);
                    } catch (Exception e) {
                        log.warn("Skipping experience entry due to error: {}", e.getMessage());
                    }
                }
            }

            // Education
            JsonNode education = root.get("education");
            if (education != null && education.isArray()) {
                int sortOrder = 0;
                for (JsonNode edu : education) {
                    try {
                        ResumeEducation entry = ResumeEducation.create(
                                resume,
                                getTextOrNull(edu, "institution"),
                                getTextOrNull(edu, "degree"),
                                getTextOrNull(edu, "fieldOfStudy"),
                                parseDate(getTextOrNull(edu, "startDate")),
                                parseDate(getTextOrNull(edu, "endDate")),
                                getTextOrNull(edu, "description"),
                                sortOrder++
                        );
                        educationRepository.save(entry);
                    } catch (Exception e) {
                        log.warn("Skipping education entry due to error: {}", e.getMessage());
                    }
                }
            }

            // Skills
            JsonNode skills = root.get("skills");
            if (skills != null && skills.isArray()) {
                int sortOrder = 0;
                for (JsonNode skill : skills) {
                    try {
                        String skillName = skill.asText().trim();
                        if (skillName.isBlank()) continue;
                        String normalized = skillName.toLowerCase();
                        if (skillRepository.existsByResume_IdAndSkillName(
                                resume.getId(), normalized)) continue;
                        ResumeSkill entry = ResumeSkill.create(
                                resume, skillName, SkillLevel.INTERMEDIATE, sortOrder++);
                        skillRepository.save(entry);
                    } catch (Exception e) {
                        log.warn("Skipping skill entry due to error: {}", e.getMessage());
                    }
                }
            }

            // Languages
            JsonNode languages = root.get("languages");
            if (languages != null && languages.isArray()) {
                int sortOrder = 0;
                for (JsonNode lang : languages) {
                    try {
                        String languageName = getTextOrNull(lang, "language");
                        if (languageName == null || languageName.isBlank()) continue;
                        String normalized = languageName.toLowerCase();
                        if (languageRepository.existsByResume_IdAndLanguageName(
                                resume.getId(), normalized)) continue;
                        LanguageLevel level = parseLanguageLevel(
                                getTextOrNull(lang, "level"));
                        ResumeLanguage entry = ResumeLanguage.create(
                                resume, languageName, level, sortOrder++);
                        languageRepository.save(entry);
                    } catch (Exception e) {
                        log.warn("Skipping language entry due to error: {}", e.getMessage());
                    }
                }
            }

        } catch (Exception e) {
            log.error("Failed to store parsed CV data: {}", e.getMessage());
        }
    }

    private String getTextOrNull(JsonNode node, String field) {
        if (!node.has(field) || node.get(field).isNull()) return null;
        String value = node.get(field).asText().trim();
        return value.isBlank() ? null : value;
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return null;
        try {
            YearMonth ym = YearMonth.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM"));
            return ym.atDay(1);
        } catch (DateTimeParseException e) {
            try {
                return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } catch (DateTimeParseException ex) {
                log.warn("Could not parse date: {}", dateStr);
                return null;
            }
        }
    }

    private LanguageLevel parseLanguageLevel(String level) {
        if (level == null) return null;
        try {
            return LanguageLevel.valueOf(level.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}