package com.jobhub.dto.resume;

import com.jobhub.entity.ResumeSkill;
import com.jobhub.enums.SkillLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResumeSkillResponse {

    private Long id;
    private String skillName;
    private String displayName;
    private SkillLevel skillLevel;
    private Integer sortOrder;

    public static ResumeSkillResponse from(ResumeSkill skill) {
        return ResumeSkillResponse.builder()
                .id(skill.getId())
                .skillName(skill.getSkillName())
                .displayName(skill.getDisplayName())
                .skillLevel(skill.getSkillLevel())
                .sortOrder(skill.getSortOrder())
                .build();
    }
}
