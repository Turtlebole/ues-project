package com.ues.repository;

import com.ues.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {

    @Query("SELECT l FROM Location l WHERE " +
           "LOWER(l.name) LIKE LOWER(CONCAT('%', :name, '%')) AND " +
           "LOWER(l.address) LIKE LOWER(CONCAT('%', :address, '%')) AND " +
           "LOWER(l.type) LIKE LOWER(CONCAT('%', :type, '%'))")
    List<Location> searchLocations(
            @Param("name") String name,
            @Param("address") String address,
            @Param("type") String type);

    @Query("SELECT DISTINCT l FROM Location l JOIN l.managers m WHERE m.user.id = :userId")
    List<Location> findByManagerId(@Param("userId") Long userId);
}
