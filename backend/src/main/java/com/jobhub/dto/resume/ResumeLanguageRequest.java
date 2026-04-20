package com.jobhub.dto.resume;

import com.jobhub.enums.LanguageLevel;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class ResumeLanguageRequest {

    @NotBlank(message = "Language name cannot be empty")
    private String languageName;
    private LanguageLevel languageLevel;
    private Integer sortOrder;
}
