package com.ues.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ReviewDTO {
    private Long id;
    private Long userId;
    private String userFirstName;
    private String userLastName;
    private Long locationId;
    private Long eventId;
    private String eventName;
    private String comment;
    private LocalDateTime createdAt;
    private boolean hidden;
    private boolean deleted;
    private RateDTO rate;
    private List<CommentDTO> comments;
    private long eventOccurrenceCount;
}
