package com.jobhub.repository;

import com.jobhub.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface ResumeRepository extends JpaRepository<Resume, Long> {
    Optional<Resume> findByCandidate_Id(Long candidateId);
    boolean existsByCandidate_Id(Long candidateId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE resumes SET embedding = CAST(:embedding AS vector) WHERE id = :id", nativeQuery = true)
    void updateEmbedding(@Param("id") Long id, @Param("embedding") String embedding);

    @Query(value = "SELECT embedding::text FROM resumes WHERE id = :id", nativeQuery = true)
    String findEmbeddingById(@Param("id") Long id);
}
