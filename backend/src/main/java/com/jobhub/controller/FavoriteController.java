package com.jobhub.controller;

import com.jobhub.dto.favorite.FavoriteResponse;
import com.jobhub.security.AuthenticatedUser;
import com.jobhub.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/candidate/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping("/{jobPostId}")
    public ResponseEntity<FavoriteResponse> addFavorite(
            @PathVariable Long jobPostId,
            @AuthenticationPrincipal AuthenticatedUser currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(favoriteService.addFavorite(jobPostId, currentUser));
    }

    @DeleteMapping("/{favoriteId}")
    public ResponseEntity<Void> removeFavorite(
            @PathVariable Long favoriteId,
            @AuthenticationPrincipal AuthenticatedUser currentUser) {
        favoriteService.removeFavorite(favoriteId, currentUser);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<FavoriteResponse>> getMyFavorites(
            @AuthenticationPrincipal AuthenticatedUser currentUser) {
        return ResponseEntity.ok(favoriteService.getMyFavorites(currentUser));
    }

    @GetMapping("/check/{jobPostId}")
    public ResponseEntity<Boolean> isFavorite(
            @PathVariable Long jobPostId,
            @AuthenticationPrincipal AuthenticatedUser currentUser) {
        return ResponseEntity.ok(favoriteService.isFavorite(jobPostId, currentUser));
    }
}
