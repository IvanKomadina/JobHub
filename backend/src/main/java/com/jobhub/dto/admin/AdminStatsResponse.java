package com.jobhub.dto.admin;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminStatsResponse {
    private long totalCandidates;
    private long totalEmployers;
    private long activePosts;
    private long totalPosts;
    private long totalApplications;
}
