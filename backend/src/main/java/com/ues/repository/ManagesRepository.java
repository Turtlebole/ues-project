package com.ues.repository;

import com.ues.model.Location;
import com.ues.model.Manages;
import com.ues.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ManagesRepository extends JpaRepository<Manages, Long> {
    List<Manages> findByLocation(Location location);
    List<Manages> findByUser(User user);
    Optional<Manages> findByUserAndLocation(User user, Location location);
    boolean existsByUserAndLocation(User user, Location location);
}
