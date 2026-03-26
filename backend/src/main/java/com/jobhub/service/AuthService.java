package com.jobhub.service;

import com.jobhub.dto.auth.AuthResponse;
import com.jobhub.dto.auth.LoginRequest;
import com.jobhub.dto.auth.RegisterRequest;
import com.jobhub.entity.Candidate;
import com.jobhub.entity.Employer;
import com.jobhub.entity.RefreshToken;
import com.jobhub.entity.User;
import com.jobhub.enums.UserRole;
import com.jobhub.repository.CandidateRepository;
import com.jobhub.repository.EmployerRepository;
import com.jobhub.repository.RefreshTokenRepository;
import com.jobhub.repository.UserRepository;
import com.jobhub.security.CookieService;
import com.jobhub.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final CandidateRepository candidateRepository;
    private final EmployerRepository employerRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final CookieService cookieService;
    private final PasswordEncoder passwordEncoder;

    // REGISTER

    @Transactional
    public AuthResponse register(RegisterRequest request, HttpServletResponse response) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already in use");
        }

        validateRoleSpecificFields(request);

        User user = User.create(
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getRole()
        );
        userRepository.save(user);

        if (request.getRole() == UserRole.CANDIDATE) {
            Candidate candidate = Candidate.create(
                    user,
                    request.getFirstName(),
                    request.getLastName()
            );
            candidateRepository.save(candidate);
        } else if (request.getRole() == UserRole.EMPLOYER) {
            Employer employer = Employer.create(
                    user,
                    request.getCompanyName()
            );
            employerRepository.save(employer);
        }

        issueTokens(user, response);

        return AuthResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    // LOGIN

    @Transactional
    public AuthResponse login( LoginRequest request, HttpServletResponse response) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!user.getIsActive())
            throw new IllegalStateException("Account is deactivated");

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash()))
            throw new IllegalArgumentException("Invalid email or password");

        refreshTokenRepository.revokeAllUserTokens(user.getId());
        issueTokens(user, response);

        return AuthResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    // REFRESH

    @Transactional
    public AuthResponse refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractRefreshTokenFromCookie(request);

        if (refreshToken == null)
            throw new IllegalStateException("Refresh token not found");

        RefreshToken storedToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new IllegalStateException("Invalid refresh token"));

        if (storedToken.getRevoked()) {
            User user = storedToken.getUser();
            refreshTokenRepository.revokeAllUserTokens(user.getId());
            throw new IllegalStateException("Refresh token reuse detected. All sessions revoked.");
        }

        if (storedToken.isExpired())
            throw new IllegalStateException("Refresh token is expired");

        User user = storedToken.getUser();

        if (!user.getIsActive())
            throw new IllegalStateException("Account is deactivated");

        storedToken.revoke();
        refreshTokenRepository.save(storedToken);

        issueTokens(user, response);

        return AuthResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    // LOGOUT

    @Transactional
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractRefreshTokenFromCookie(request);

        if (refreshToken != null) {
            refreshTokenRepository.findByToken(refreshToken)
                    .ifPresent(token -> {
                        token.revoke();
                        refreshTokenRepository.save(token);
                    });
        }

        cookieService.clearAuthCookies(response);
    }

    // PRIVATE HELPERS

    private void issueTokens(User user, HttpServletResponse response) {
        String accessToken = jwtService.generateAccessToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name()
        );

        String refreshTokenValue = jwtService.generateRefreshToken();
        LocalDateTime expiresAt = LocalDateTime.now()
                .plusSeconds(jwtService.getRefreshExpiration() / 1000);

        RefreshToken refreshToken = RefreshToken.create(user, refreshTokenValue, expiresAt);
        refreshTokenRepository.save(refreshToken);

        cookieService.addAccessTokenCookie(response, accessToken, jwtService.getExpiration());
        cookieService.addRefreshCookie(response, refreshTokenValue, jwtService.getRefreshExpiration());
    }

    private void validateRoleSpecificFields(RegisterRequest request) {
        if (request.getRole() == UserRole.CANDIDATE) {
            if (request.getFirstName() == null || request.getFirstName().isBlank())
                throw new IllegalArgumentException("First name is required for candidates");
            if (request.getLastName() == null || request.getLastName().isBlank())
                throw new IllegalArgumentException("Last name is required for candidates");
        } else if (request.getRole() == UserRole.EMPLOYER) {
            if (request.getCompanyName() == null || request.getCompanyName().isBlank())
                throw new IllegalArgumentException("Company name is required for employers");
        } else {
            throw new IllegalArgumentException("Cannot register with this role");
        }
    }

    private String extractRefreshTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null)
            return null;

        return Arrays.stream(request.getCookies())
                .filter(cookie -> "refresh_token".equals(cookie.getName()))
                .map(jakarta.servlet.http.Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}