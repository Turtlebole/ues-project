package com.ues.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommentDTO {
    private Long id;
    private Long reviewId;
    private Long userId;
    private String userFirstName;
    private String userLastName;
    private String userRole;
    private String text;
    private LocalDateTime createdAt;
    private Long parentCommentId;
    private List<CommentDTO> replies;
}
