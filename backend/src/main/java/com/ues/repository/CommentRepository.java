package com.ues.repository;

import com.ues.model.Comment;
import com.ues.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByReviewAndParentCommentIsNull(Review review);
}
