package com.jobhub.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "resumes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Resume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false, unique = true)
    private Candidate candidate;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder(access = AccessLevel.PRIVATE)
    private Resume(Candidate candidate, String summary) {
        this.candidate = candidate;
        this.summary = summary;
    }

    public static Resume create(Candidate candidate, String summary) {
        if (candidate == null) throw new IllegalArgumentException("Candidate cannot be null");
        if (summary != null && summary.length() > 2000) {
            throw new IllegalArgumentException("Summary too long");
        }
        return Resume.builder()
                .candidate(candidate)
                .summary(summary)
                .build();
    }

    public void updateSummary(String summary) {
        if (summary != null && summary.length() > 2000) {
            throw new IllegalArgumentException("Summary too long");
        }
        this.summary = summary;
    }
}
