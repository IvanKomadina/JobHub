package com.jobhub.service;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.jobhub.entity.*;
import com.jobhub.exception.ResourceNotFoundException;
import com.jobhub.repository.*;
import com.jobhub.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ResumePdfService {

    private final ResumeRepository resumeRepository;
    private final ResumeEducationRepository educationRepository;
    private final ResumeExperienceRepository experienceRepository;
    private final ResumeSkillRepository skillRepository;
    private final ResumeLanguageRepository languageRepository;
    private final CandidateRepository candidateRepository;

    private static final DeviceRgb PRIMARY_COLOR = new DeviceRgb(30, 64, 175);
    private static final DeviceRgb LIGHT_GRAY = new DeviceRgb(243, 244, 246);
    private static final DeviceRgb TEXT_GRAY = new DeviceRgb(107, 114, 128);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMM yyyy");

    @Transactional(readOnly = true)
    public byte[] generateResumePdf(AuthenticatedUser currentUser) {
        Candidate candidate = candidateRepository.findByUser_Id(currentUser.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found"));

        Resume resume = resumeRepository.findByCandidate_Id(candidate.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found"));

        List<ResumeEducation> education = educationRepository
                .findByResume_IdOrderBySortOrderAsc(resume.getId());
        List<ResumeExperience> experience = experienceRepository
                .findByResume_IdOrderBySortOrderAsc(resume.getId());
        List<ResumeSkill> skills = skillRepository
                .findByResume_IdOrderBySortOrderAsc(resume.getId());
        List<ResumeLanguage> languages = languageRepository
                .findByResume_IdOrderBySortOrderAsc(resume.getId());

        return buildPdf(candidate, resume, education, experience, skills, languages);
    }

    private byte[] buildPdf(Candidate candidate, Resume resume,
                            List<ResumeEducation> education,
                            List<ResumeExperience> experience,
                            List<ResumeSkill> skills,
                            List<ResumeLanguage> languages) {

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument, PageSize.A4);
            document.setMargins(40, 50, 40, 50);

            PdfFont bold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont regular = PdfFontFactory.createFont(StandardFonts.HELVETICA);

            addHeader(document, candidate, bold, regular);
            addDivider(document);

            if (resume.getSummary() != null && !resume.getSummary().isBlank()) {
                addSection(document, "Summary", bold);
                addBodyText(document, resume.getSummary(), regular);
            }

            if (!experience.isEmpty()) {
                addSection(document, "Work Experience", bold);
                experience.forEach((exp) -> addExperienceEntry(document, exp, bold, regular));
            }

            if (!education.isEmpty()) {
                addSection(document, "Education", bold);
                education.forEach((edu) -> addEducationEntry(document, edu, bold, regular) );
            }

            if (!skills.isEmpty()) {
                addSection(document, "Skills", bold);
                addSkills(document, skills, regular);
            }

            if (!languages.isEmpty()) {
                addSection(document, "Languages", bold);
                addLanguages(document, languages, regular);
            }

            document.close();
            return outputStream.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Failed to generate PDF: " + e);
        }
    }

    private void addHeader(Document document, Candidate candidate, PdfFont bold, PdfFont regular) {
        String fullName = candidate.getFirstName() + " " + candidate.getLastName();

        Paragraph fullNameParagraph = new Paragraph(fullName)
                .setFont(bold)
                .setFontSize(24)
                .setFontColor(PRIMARY_COLOR)
                .setTextAlignment(TextAlignment.LEFT)
                .setMarginBottom(4);

        document.add(fullNameParagraph);

        StringBuilder contactLine = new StringBuilder();
        contactLine.append(candidate.getUser().getEmail());
        if (candidate.getPhone() != null)
            contactLine.append(" • ").append(candidate.getPhone());
        if (candidate.getLocation() != null)
            contactLine.append(" • ").append(candidate.getLocation());

        document.add(new Paragraph(contactLine.toString()))
                .setFont(regular)
                .setFontSize(9)
                .setFontColor(TEXT_GRAY)
                .setTextAlignment(TextAlignment.LEFT)
                .setBottomMargin(8);
    }

    private void addSection(Document document, String title, PdfFont bold) {
        document.add(new Paragraph(title.toUpperCase())
                .setFont(bold)
                .setFontSize(10)
                .setFontColor(PRIMARY_COLOR)
                .setMarginTop(14)
                .setMarginBottom(4));

        addDivider(document);
    }

    private void addBodyText(Document document, String text, PdfFont regular) {
        document.add(new Paragraph(text))
                .setFont(regular)
                .setFontSize(9)
                .setFontColor(ColorConstants.BLACK)
                .setBottomMargin(6);
    }

    private void addDivider(Document document) {
        Table divider = new Table(1)
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(6);

        divider.addCell(new Cell()
                .setHeight(1)
                .setBackgroundColor(LIGHT_GRAY)
                .setBorder(null));

        document.add(divider);
    }

    private void addExperienceEntry(Document document, ResumeExperience experience,
                                    PdfFont bold, PdfFont regular) {
        String dateRange = formatDateRange(
                experience.getStartDate() != null ? experience.getStartDate().format(DATE_FORMAT) : "",
                experience.isCurrent() ? "Present" : (experience.getEndDate() != null
                        ? experience.getEndDate().format(DATE_FORMAT) : "")
        );

        Table table = new Table(UnitValue.createPercentArray(new float[]{70, 30}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(2);

        table.addCell(new Cell().add(new Paragraph(experience.getPosition())
                        .setFont(bold).setFontSize(10))
                .setBorder(null).setPadding(0));

        table.addCell(new Cell().add(new Paragraph(dateRange)
                    .setFont(regular).setFontSize(9)
                    .setFontColor(TEXT_GRAY)
                    .setTextAlignment(TextAlignment.RIGHT))
                .setBorder(null).setPadding(0));

        document.add(table);

        StringBuilder subtitle = new StringBuilder(experience.getCompany());
        if (experience.getLocation() != null)
            subtitle.append(" • ").append(experience.getLocation());

        document.add(new Paragraph(subtitle.toString())
                .setFont(regular)
                .setFontSize(9)
                .setFontColor(TEXT_GRAY)
                .setMarginBottom(3));

        if (experience.getDescription() != null && !experience.getDescription().isBlank()) {
            addBodyText(document, experience.getDescription(), regular);
        }
    }

    private void addEducationEntry(Document document, ResumeEducation education,
                                   PdfFont bold, PdfFont regular) {
        String dateRange = formatDateRange(
                education.getStartDate() != null ? education.getStartDate().format(DATE_FORMAT) : "",
                education.getEndDate() != null ? education.getEndDate().format(DATE_FORMAT) : ""
        );

        Table table = new Table(UnitValue.createPercentArray(new float[]{70, 30}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(2);

        String degreeText = education.getDegree() != null ? education.getDegree() : education.getInstitution();
        if (education.getDegree() != null && education.getFieldOfStudy() != null)
            degreeText = education.getDegree() + " in " + education.getFieldOfStudy();

        table.addCell(new Cell().add(new Paragraph(degreeText)
                        .setFont(bold).setFontSize(10))
                .setBorder(null).setPadding(0));

        table.addCell(new Cell().add(new Paragraph(dateRange)
                        .setFont(regular).setFontSize(9)
                        .setFontColor(TEXT_GRAY)
                        .setTextAlignment(TextAlignment.RIGHT))
                .setBorder(null).setPadding(0));

        document.add(table);

        document.add(new Paragraph(education.getInstitution())
                .setFont(regular)
                .setFontSize(9)
                .setFontColor(TEXT_GRAY)
                .setMarginBottom(3));

        if (education.getDescription() != null && !education.getDescription().isBlank()) {
            addBodyText(document, education.getDescription(), regular);
        }
    }

    private void addSkills(Document document, List<ResumeSkill> skills, PdfFont regular) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < skills.size(); i++) {
            ResumeSkill skill = skills.get(i);
            sb.append(skill.getDisplayName());
            if (skill.getSkillLevel() != null)
                sb.append(" (").append(formatEnum(skill.getSkillLevel().name())).append(")");
            if (i < skills.size() - 1)
                sb.append("  •  ");
        }
        document.add(new Paragraph(sb.toString())
                .setFont(regular)
                .setFontSize(9)
                .setMarginBottom(4));
    }

    private void addLanguages(Document document, List<ResumeLanguage> languages, PdfFont regular) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < languages.size(); i++) {
            ResumeLanguage language = languages.get(i);
            sb.append(language.getLanguageName());
            if (language.getLanguageLevel() != null)
                sb.append(" (").append(language.getLanguageLevel().name()).append(")");
            if (i < languages.size() - 1)
                sb.append("  •  ");
        }
        document.add(new Paragraph(sb.toString())
                .setFont(regular)
                .setFontSize(9)
                .setMarginBottom(4));
    }

    private String formatDateRange(String start, String end) {
        if (start.isBlank() && end.isBlank()) return "";
        if (start.isBlank()) return end;
        if (end.isBlank()) return start;
        return start + " - " + end;
    }

    private String formatEnum(String value) {
        if (value == null) return "";
        return value.charAt(0) + value.substring(1).toLowerCase();
    }
}
