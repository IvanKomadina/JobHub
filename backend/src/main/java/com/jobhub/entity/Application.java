package com.jobhub.entity;

import com.jobhub.enums.ApplicationStatus;
import com.jobhub.enums.PostStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "applications",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_application_candidate_post",
                columnNames = {"job_post_id", "candidate_id"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_post_id", nullable = false)
    private JobPost jobPost;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ApplicationStatus status;

    @Column(name = "cover_letter", columnDefinition = "TEXT")
    private String coverLetter;

    @CreationTimestamp
    @Column(name = "applied_at", nullable = false, updatable = false)
    private LocalDateTime appliedAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder(access = AccessLevel.PRIVATE)
    private Application(JobPost jobPost, Candidate candidate, String coverLetter) {
        this.jobPost = jobPost;
        this.candidate = candidate;
        this.coverLetter = coverLetter;
        this.status = ApplicationStatus.PENDING;
    }

    public static Application create(JobPost jobPost, Candidate candidate, String coverLetter) {
        if (jobPost == null) throw new IllegalArgumentException("Job post cannot be null");
        if (candidate == null) throw new IllegalArgumentException("Candidate cannot be null");
        if (jobPost.getStatus() != PostStatus.ACTIVE)
            throw new IllegalStateException("Cannot apply to a post that is not active");

        return Application.builder()
                .jobPost(jobPost)
                .candidate(candidate)
                .coverLetter(coverLetter)
                .build();
    }

    public void accept() {
        if (this.status != ApplicationStatus.PENDING)
            throw new IllegalStateException("Only pending applications can be accepted");
        this.status = ApplicationStatus.ACCEPTED;
    }

    public void reject() {
        if (this.status != ApplicationStatus.PENDING)
            throw new IllegalStateException("Only pending applications can be rejected");
        this.status = ApplicationStatus.REJECTED;
    }

    public void withdraw() {
        if (this.status != ApplicationStatus.PENDING)
            throw new IllegalStateException("Only pending applications can be withdrawn");
        this.status = ApplicationStatus.WITHDRAWN;
    }
}
