package com.jobhub.service;

import com.jobhub.dto.favorite.FavoriteResponse;
import com.jobhub.entity.Candidate;
import com.jobhub.entity.Favorite;
import com.jobhub.entity.JobPost;
import com.jobhub.enums.PostStatus;
import com.jobhub.exception.AccessDeniedException;
import com.jobhub.exception.ResourceNotFoundException;
import com.jobhub.repository.CandidateRepository;
import com.jobhub.repository.FavoriteRepository;
import com.jobhub.repository.JobPostRepository;
import com.jobhub.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final CandidateRepository candidateRepository;
    private final JobPostRepository jobPostRepository;

    @Transactional
    public FavoriteResponse addFavorite(Long jobPostId, AuthenticatedUser currentUser) {
        Candidate candidate = getCandidateByUserId(currentUser.getUserId());

        JobPost jobPost = jobPostRepository.findById(jobPostId)
                .orElseThrow(() -> new ResourceNotFoundException("Job post not found"));

        if (jobPost.getStatus() != PostStatus.ACTIVE)
            throw new IllegalStateException("Cannot save a post that is not active");

        if (favoriteRepository.existsByCandidate_IdAndJobPost_Id(candidate.getId(), jobPostId))
            throw new IllegalStateException("Job post is already in favorites");

        Favorite favorite = Favorite.create(candidate, jobPost);
        favoriteRepository.save(favorite);
        return FavoriteResponse.from(favorite);
    }

    @Transactional
    public void removeFavorite(Long favoriteId, AuthenticatedUser currentUser) {
        Candidate candidate = getCandidateByUserId(currentUser.getUserId());

        Favorite favorite = favoriteRepository.findById(favoriteId)
                .orElseThrow(() -> new ResourceNotFoundException("Favorite not found"));

        if (!favorite.getCandidate().getId().equals(candidate.getId()))
            throw new AccessDeniedException("You do not have permission to remove this favorite");

        favoriteRepository.delete(favorite);
    }

    @Transactional(readOnly = true)
    public List<FavoriteResponse> getMyFavorites(AuthenticatedUser currentUser) {
        Candidate candidate = getCandidateByUserId(currentUser.getUserId());

        return favoriteRepository.findByCandidate_Id(candidate.getId())
                .stream()
                .map(FavoriteResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean isFavorite(Long jobPostId ,AuthenticatedUser currentUser) {
        Candidate candidate = getCandidateByUserId(currentUser.getUserId());
        return favoriteRepository.existsByCandidate_IdAndJobPost_Id(candidate.getId(), jobPostId);
    }

    private Candidate getCandidateByUserId(Long userId) {
        return candidateRepository.findByUser_Id(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found"));
    }
}
