package com.jobhub.dto.resume;

import com.jobhub.enums.SkillLevel;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class ResumeSkillRequest {

    @NotBlank(message = "Skill name cannot be empty")
    private String skillName;
    private SkillLevel skillLevel;
    private Integer sortOrder;
}
