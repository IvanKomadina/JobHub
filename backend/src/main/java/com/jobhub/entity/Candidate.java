package com.jobhub.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "candidates")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Candidate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(length = 30)
    private String phone;

    @Column(length = 255)
    private String location;

    @Column(name = "profile_picture", length = 512)
    private String profilePicture;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder(access = AccessLevel.PRIVATE)
    private Candidate(User user, String firstName, String lastName) {
        this.user = user;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public static Candidate create(User user, String firstName, String lastName) {
        if (user == null) throw new IllegalArgumentException("User cannot be null");
        if (firstName == null || firstName.isBlank()) throw new IllegalArgumentException("First name cannot be empty");
        if (lastName == null || lastName.isBlank()) throw new IllegalArgumentException("Last name cannot be empty");

        return Candidate.builder()
                .user(user)
                .firstName(firstName)
                .lastName(lastName)
                .build();
    }

    public void updateProfile(String firstName, String lastName, String phone, String location, String bio) {
        if (firstName == null || firstName.isBlank()) throw new IllegalArgumentException("First name cannot be empty");
        if (lastName == null || lastName.isBlank()) throw new IllegalArgumentException("Last name cannot be empty");
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.location = location;
        this.bio = bio;
    }

    public void updateProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }
}
