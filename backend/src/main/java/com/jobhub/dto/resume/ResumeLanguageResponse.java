package com.jobhub.dto.resume;

import com.jobhub.entity.ResumeLanguage;
import com.jobhub.enums.LanguageLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResumeLanguageResponse {

    private Long id;
    private String languageName;
    private LanguageLevel languageLevel;
    private Integer sortOrder;

    public static ResumeLanguageResponse from(ResumeLanguage language) {
        return ResumeLanguageResponse.builder()
                .id(language.getId())
                .languageName(language.getLanguageName())
                .languageLevel(language.getLanguageLevel())
                .sortOrder(language.getSortOrder())
                .build();
    }
}
