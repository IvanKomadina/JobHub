package com.jobhub.dto.jobpost;

import com.jobhub.enums.EmploymentType;
import com.jobhub.enums.PostStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JobPostFilterRequest {
    private String keyword;
    private Long categoryId;
    private Long locationId;
    private EmploymentType employmentType;
    private PostStatus postStatus;
    private Integer page = 0;
    private Integer size = 10;
    private String sortBy = "publishedAt";
    private String sortDirection = "desc";
}
