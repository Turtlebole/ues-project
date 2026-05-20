package com.ues.repository;

import com.ues.model.Location;
import com.ues.model.Review;
import com.ues.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query("SELECT r FROM Review r WHERE r.location = :location AND r.deleted = false ORDER BY r.createdAt DESC")
    List<Review> findByLocationNotDeleted(@Param("location") Location location);

    @Query("SELECT r FROM Review r WHERE r.user = :user AND r.deleted = false")
    List<Review> findByUserNotDeleted(@Param("user") User user);

    @Query("SELECT r FROM Review r WHERE r.location = :location AND r.deleted = false " +
           "ORDER BY r.createdAt DESC LIMIT 3")
    List<Review> findTop3ByLocationOrderByCreatedAtDesc(@Param("location") Location location);

    @Query("SELECT AVG(" +
           "COALESCE(ra.performanceRating, 0) + COALESCE(ra.soundLightRating, 0) + " +
           "COALESCE(ra.spaceRating, 0) + COALESCE(ra.overallRating, 0)) " +
           "FROM Review r JOIN r.rate ra " +
           "WHERE r.location = :location AND r.deleted = false")
    Double calculateAverageRatingForLocation(@Param("location") Location location);

    boolean existsByUserAndEvent(User user, com.ues.model.Event event);
}
