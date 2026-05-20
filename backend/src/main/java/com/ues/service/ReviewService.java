package com.ues.service;

import com.ues.dto.CommentDTO;
import com.ues.dto.RateDTO;
import com.ues.dto.ReviewDTO;
import com.ues.model.*;
import com.ues.repository.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    private static final Logger logger = LogManager.getLogger(ReviewService.class);

    private final ReviewRepository reviewRepository;
    private final LocationRepository locationRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final RateRepository rateRepository;
    private final CommentRepository commentRepository;
    private final ManagesRepository managesRepository;

    public ReviewService(ReviewRepository reviewRepository, LocationRepository locationRepository,
                         EventRepository eventRepository, UserRepository userRepository,
                         RateRepository rateRepository, CommentRepository commentRepository,
                         ManagesRepository managesRepository) {
        this.reviewRepository = reviewRepository;
        this.locationRepository = locationRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.rateRepository = rateRepository;
        this.commentRepository = commentRepository;
        this.managesRepository = managesRepository;
    }

    public List<ReviewDTO> getReviewsByLocation(Long locationId, String sortBy, String sortDir) {
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new RuntimeException("Location not found"));
        List<Review> reviews = reviewRepository.findByLocationNotDeleted(location);

        Comparator<ReviewDTO> comparator = switch (sortBy != null ? sortBy : "date") {
            case "rating" -> Comparator.comparingDouble(r -> {
                if (r.getRate() == null) return 0.0;
                RateDTO rate = r.getRate();
                double sum = 0;
                int count = 0;
                if (rate.getPerformanceRating() != null) { sum += rate.getPerformanceRating(); count++; }
                if (rate.getSoundLightRating() != null) { sum += rate.getSoundLightRating(); count++; }
                if (rate.getSpaceRating() != null) { sum += rate.getSpaceRating(); count++; }
                if (rate.getOverallRating() != null) { sum += rate.getOverallRating(); count++; }
                return count > 0 ? sum / count : 0.0;
            });
            default -> Comparator.comparing(ReviewDTO::getCreatedAt);
        };

        List<ReviewDTO> dtos = reviews.stream().map(this::toDTO).collect(Collectors.toList());
        if ("desc".equalsIgnoreCase(sortDir)) {
            dtos.sort(comparator.reversed());
        } else {
            dtos.sort(comparator);
        }
        return dtos;
    }

    @Transactional
    public ReviewDTO createReview(Long userId, Long locationId, Long eventId, String comment,
                                   Integer performanceRating, Integer soundLightRating,
                                   Integer spaceRating, Integer overallRating) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Location location = locationRepository.findById(locationId).orElseThrow(() -> new RuntimeException("Location not found"));
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new RuntimeException("Event not found"));

        if (!event.isRegular()) {
            throw new RuntimeException("Reviews can only be left for regular events");
        }
        if (event.getDate().isAfter(LocalDateTime.now())) {
            throw new RuntimeException("Event has not taken place yet");
        }
        if (!event.getLocation().getId().equals(locationId)) {
            throw new RuntimeException("Event does not belong to this location");
        }

        Review review = Review.builder()
                .user(user)
                .location(location)
                .event(event)
                .comment(comment)
                .createdAt(LocalDateTime.now())
                .hidden(false)
                .deleted(false)
                .build();
        review = reviewRepository.save(review);

        Rate rate = Rate.builder()
                .review(review)
                .performanceRating(performanceRating)
                .soundLightRating(soundLightRating)
                .spaceRating(spaceRating)
                .overallRating(overallRating)
                .build();
        rateRepository.save(rate);

        logger.info("Review created by user {} for location {}", userId, locationId);
        return toDTO(review);
    }

    @Transactional
    public void hideReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId).orElseThrow(() -> new RuntimeException("Review not found"));
        review.setHidden(!review.isHidden());
        reviewRepository.save(review);
        logger.info("Review {} hidden status toggled to: {}", reviewId, review.isHidden());
    }

    @Transactional
    public void deleteReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId).orElseThrow(() -> new RuntimeException("Review not found"));
        review.setDeleted(true);
        reviewRepository.save(review);
        logger.info("Review {} soft deleted", reviewId);
    }

    @Transactional
    public CommentDTO addComment(Long reviewId, Long userId, String text, Long parentCommentId) {
        Review review = reviewRepository.findById(reviewId).orElseThrow(() -> new RuntimeException("Review not found"));
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        Comment parentComment = null;
        if (parentCommentId != null) {
            parentComment = commentRepository.findById(parentCommentId)
                    .orElseThrow(() -> new RuntimeException("Parent comment not found"));
            // Validate: if parent is a top-level comment by manager, any user can reply
            // If it's a reply, check it's a manager's comment at root level
            boolean isManagerReply = isManagerComment(parentComment, review.getLocation());
            if (!isManagerReply && parentComment.getParentComment() == null) {
                // Only managers can reply to top-level user comments
                if (!managesRepository.existsByUserAndLocation(user, review.getLocation())) {
                    throw new RuntimeException("Only managers can reply to user reviews directly");
                }
            }
        } else {
            // Top-level comment must be from manager
            if (!managesRepository.existsByUserAndLocation(user, review.getLocation())) {
                throw new RuntimeException("Only managers can add top-level comments on reviews");
            }
        }

        Comment comment = Comment.builder()
                .review(review)
                .user(user)
                .text(text)
                .createdAt(LocalDateTime.now())
                .parentComment(parentComment)
                .build();
        comment = commentRepository.save(comment);
        logger.info("Comment added to review {} by user {}", reviewId, userId);
        return toCommentDTO(comment);
    }

    private boolean isManagerComment(Comment comment, Location location) {
        return managesRepository.existsByUserAndLocation(comment.getUser(), location);
    }

    public List<ReviewDTO> getTop3ReviewsForMostPopularLocation() {
        List<Location> all = locationRepository.findAll();
        Location mostPopular = all.stream()
                .max(Comparator.comparingDouble(l -> {
                    Double avg = reviewRepository.calculateAverageRatingForLocation(l);
                    return avg != null ? avg : 0.0;
                }))
                .orElse(null);
        if (mostPopular == null) return List.of();
        return reviewRepository.findTop3ByLocationOrderByCreatedAtDesc(mostPopular)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public ReviewDTO toDTO(Review review) {
        ReviewDTO dto = new ReviewDTO();
        dto.setId(review.getId());
        dto.setUserId(review.getUser().getId());
        dto.setUserFirstName(review.getUser().getFirstName());
        dto.setUserLastName(review.getUser().getLastName());
        dto.setLocationId(review.getLocation().getId());
        dto.setEventId(review.getEvent().getId());
        dto.setEventName(review.getEvent().getName());
        dto.setComment(review.getComment());
        dto.setCreatedAt(review.getCreatedAt());
        dto.setHidden(review.isHidden());
        dto.setDeleted(review.isDeleted());

        if (review.getRate() != null) {
            RateDTO rateDTO = new RateDTO();
            rateDTO.setId(review.getRate().getId());
            rateDTO.setPerformanceRating(review.getRate().getPerformanceRating());
            rateDTO.setSoundLightRating(review.getRate().getSoundLightRating());
            rateDTO.setSpaceRating(review.getRate().getSpaceRating());
            rateDTO.setOverallRating(review.getRate().getOverallRating());
            dto.setRate(rateDTO);
        }

        List<Comment> topLevelComments = commentRepository.findByReviewAndParentCommentIsNull(review);
        dto.setComments(topLevelComments.stream().map(this::toCommentDTO).collect(Collectors.toList()));

        dto.setEventOccurrenceCount(eventRepository.countOccurrencesByName(
                review.getLocation().getId(), review.getEvent().getName(), review.getCreatedAt()));

        return dto;
    }

    private CommentDTO toCommentDTO(Comment comment) {
        CommentDTO dto = new CommentDTO();
        dto.setId(comment.getId());
        dto.setReviewId(comment.getReview().getId());
        dto.setUserId(comment.getUser().getId());
        dto.setUserFirstName(comment.getUser().getFirstName());
        dto.setUserLastName(comment.getUser().getLastName());
        dto.setUserRole(comment.getUser().getRole().name());
        dto.setText(comment.getText());
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setParentCommentId(comment.getParentComment() != null ? comment.getParentComment().getId() : null);

        if (comment.getReplies() != null && !comment.getReplies().isEmpty()) {
            dto.setReplies(comment.getReplies().stream().map(this::toCommentDTO).collect(Collectors.toList()));
        }
        return dto;
    }
}
