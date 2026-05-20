package com.ues.controller;

import com.ues.dto.CommentDTO;
import com.ues.dto.ReviewDTO;
import com.ues.security.UserDetailsImpl;
import com.ues.service.ReviewService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private static final Logger logger = LogManager.getLogger(ReviewController.class);

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReviewDTO> createReview(
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long locationId = Long.parseLong(body.get("locationId").toString());
        Long eventId = Long.parseLong(body.get("eventId").toString());
        String comment = (String) body.get("comment");
        Integer performanceRating = body.get("performanceRating") != null ?
                Integer.parseInt(body.get("performanceRating").toString()) : null;
        Integer soundLightRating = body.get("soundLightRating") != null ?
                Integer.parseInt(body.get("soundLightRating").toString()) : null;
        Integer spaceRating = body.get("spaceRating") != null ?
                Integer.parseInt(body.get("spaceRating").toString()) : null;
        Integer overallRating = body.get("overallRating") != null ?
                Integer.parseInt(body.get("overallRating").toString()) : null;

        logger.info("Creating review by user {} for location {}", userDetails.getId(), locationId);
        return ResponseEntity.ok(reviewService.createReview(userDetails.getId(), locationId, eventId,
                comment, performanceRating, soundLightRating, spaceRating, overallRating));
    }

    @PatchMapping("/{id}/hide")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<Void> hideReview(@PathVariable Long id) {
        logger.info("Toggling hide on review: {}", id);
        reviewService.hideReview(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        logger.info("Deleting review: {}", id);
        reviewService.deleteReview(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/comments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommentDTO> addComment(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        String text = (String) body.get("text");
        Long parentCommentId = body.get("parentCommentId") != null ?
                Long.parseLong(body.get("parentCommentId").toString()) : null;
        logger.info("Adding comment to review {} by user {}", id, userDetails.getId());
        return ResponseEntity.ok(reviewService.addComment(id, userDetails.getId(), text, parentCommentId));
    }

    @GetMapping("/popular-location/recent")
    public ResponseEntity<?> getRecentReviewsFromPopularLocation() {
        return ResponseEntity.ok(reviewService.getTop3ReviewsForMostPopularLocation());
    }
}
