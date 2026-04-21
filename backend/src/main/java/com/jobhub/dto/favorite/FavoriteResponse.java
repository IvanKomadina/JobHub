package com.jobhub.dto.favorite;

import com.jobhub.entity.Favorite;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class FavoriteResponse {

    private Long id;
    private LocalDateTime savedAt;

    // Job post info
    private Long jobPostId;
    private String jobPostTitle;
    private String companyName;
    private String companyLogo;
    private String city;
    private String country;

    public static FavoriteResponse from(Favorite favorite) {
        return FavoriteResponse.builder()
                .id(favorite.getId())
                .savedAt(favorite.getSavedAt())
                .jobPostId(favorite.getJobPost().getId())
                .jobPostTitle(favorite.getJobPost().getTitle())
                .companyName(favorite.getJobPost().getEmployer().getCompanyName())
                .companyLogo(favorite.getJobPost().getEmployer().getLogoUrl())
                .city(favorite.getJobPost().getLocation() != null
                        ? favorite.getJobPost().getLocation().getCity() : null)
                .country(favorite.getJobPost().getLocation() != null
                        ? favorite.getJobPost().getLocation().getCountry() : null)
                .build();
    }
}
