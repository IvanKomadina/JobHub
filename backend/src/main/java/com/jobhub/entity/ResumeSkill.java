package com.jobhub.entity;

import com.jobhub.enums.SkillLevel;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "resume_skills",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"resume_id", "skill_name"}
        ))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ResumeSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    @Column(name = "skill_name", nullable = false, length = 100)
    private String skillName;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private SkillLevel skillLevel;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Builder(access = AccessLevel.PRIVATE)
    private ResumeSkill(Resume resume, String skillName, SkillLevel skillLevel, Integer sortOrder) {
        this.resume = resume;
        this.skillName = normalize(skillName);
        this.displayName = skillName.trim();
        this.skillLevel = skillLevel;
        this.sortOrder = sortOrder != null ? sortOrder : 0;
    }

    public static ResumeSkill create(Resume resume, String skillName, SkillLevel skillLevel, Integer sortOrder) {
        if (resume == null) throw new IllegalArgumentException("Resume cannot be null");
        if (skillName == null || skillName.isBlank()) throw new IllegalArgumentException("Skill name cannot be empty");
        return ResumeSkill.builder()
                .resume(resume)
                .skillName(skillName)
                .skillLevel(skillLevel)
                .sortOrder(sortOrder)
                .build();
    }

    public void update(String skillName, SkillLevel skillLevel, Integer sortOrder) {
        if (skillName == null || skillName.isBlank()) throw new IllegalArgumentException("Skill name cannot be empty");
        this.skillName = normalize(skillName);
        this.displayName = skillName.trim();
        this.skillLevel = skillLevel;
        this.sortOrder = sortOrder != null ? sortOrder : 0;
    }

    private static String normalize(String skillName) {
        return skillName.trim().toLowerCase();
    }
}
