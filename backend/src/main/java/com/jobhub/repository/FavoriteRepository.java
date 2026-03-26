package com.jobhub.repository;

import com.jobhub.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    List<Favorite> findByCandidate_Id(Long candidateId);
    Optional<Favorite> findByCandidate_IdAndJobPost_Id(Long candidateId, Long jobPostId);
    boolean existsByCandidate_IdAndJobPost_Id(Long candidateId, Long jobPostId);
}