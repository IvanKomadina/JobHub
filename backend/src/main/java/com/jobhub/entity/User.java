package com.jobhub.entity;

import com.jobhub.enums.UserRole;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private UserRole role;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder(access = AccessLevel.PRIVATE)
    private User(String email, String passwordHash, UserRole role) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.isActive = true;
    }

    public static User create(String email, String passwordHash, UserRole role) {
        if (email == null || email.isBlank()) throw new IllegalArgumentException("Email cannot be empty");
        if (passwordHash == null || passwordHash.isBlank()) throw new IllegalArgumentException("Password cannot be empty");
        if (role == null) throw new IllegalArgumentException("Role cannot be null");

        return User.builder()
                .email(email)
                .passwordHash(passwordHash)
                .role(role)
                .build();
    }

    public void changeEmail(String newEmail) {
        if (newEmail == null || newEmail.isBlank()) throw new IllegalArgumentException("Email cannot be empty");
        this.email = newEmail;
    }

    public void changePassword(String newHash) {
        if (newHash == null || newHash.isBlank()) throw new IllegalArgumentException("Password hash cannot be empty");
        this.passwordHash = newHash;
    }

    public void changeRole(UserRole newRole) {
        if (newRole == null) throw new IllegalArgumentException("Role cannot be null");
        this.role = newRole;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void activate() {
        this.isActive = true;
    }
}