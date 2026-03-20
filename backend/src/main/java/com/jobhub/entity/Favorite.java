package com.jobhub.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "favorites",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"candidate_id", "job_post_id"}
        ))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Favorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_post_id", nullable = false)
    private JobPost jobPost;

    @CreationTimestamp
    @Column(name = "saved_at", nullable = false, updatable = false)
    private LocalDateTime savedAt;

    @Builder(access = AccessLevel.PROTECTED)
    private Favorite(Candidate candidate, JobPost jobPost) {
        this.candidate = candidate;
        this.jobPost = jobPost;
    }

    public static Favorite create(Candidate candidate, JobPost jobPost) {

        if(candidate == null) throw new IllegalArgumentException("Candidate cannot be null");
        if(jobPost == null) throw new IllegalArgumentException("Job post cannot be null");

        return Favorite.builder()
                .candidate(candidate)
                .jobPost(jobPost)
                .build();
    }
}
