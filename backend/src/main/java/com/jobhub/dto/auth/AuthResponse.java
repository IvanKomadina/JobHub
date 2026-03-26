package com.jobhub.dto.auth;

import com.jobhub.enums.UserRole;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthResponse {
    private Long userId;
    private String email;
    private UserRole role;
}
