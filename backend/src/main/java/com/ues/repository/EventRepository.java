package com.ues.repository;

import com.ues.model.Event;
import com.ues.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByLocation(Location location);

    @Query("SELECT e FROM Event e WHERE e.date >= :startOfDay AND e.date < :endOfDay")
    List<Event> findTodayEvents(
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay);

    @Query("SELECT e FROM Event e WHERE " +
           "e.date >= :startOfDay AND e.date < :endOfDay AND " +
           "LOWER(e.type) LIKE LOWER(CONCAT('%', :type, '%')) AND " +
           "(:locationId IS NULL OR e.location.id = :locationId) AND " +
           "LOWER(e.address) LIKE LOWER(CONCAT('%', :address, '%')) AND " +
           "(:maxPrice IS NULL OR (e.free = true OR e.price <= :maxPrice))")
    List<Event> searchEvents(
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay,
            @Param("type") String type,
            @Param("locationId") Long locationId,
            @Param("address") String address,
            @Param("maxPrice") Double maxPrice);

    @Query("SELECT e FROM Event e WHERE e.location = :location AND e.date < :now ORDER BY e.date DESC")
    List<Event> findPastEventsByLocation(
            @Param("location") Location location,
            @Param("now") LocalDateTime now);

    @Query("SELECT e FROM Event e WHERE e.location.id = :locationId AND e.date BETWEEN :start AND :end")
    List<Event> findByLocationIdAndDateBetween(
            @Param("locationId") Long locationId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("SELECT e FROM Event e WHERE e.location = :location AND e.regular = true AND e.date < :now")
    List<Event> findRegularPastEventsByLocation(
            @Param("location") Location location,
            @Param("now") LocalDateTime now);

    @Query("SELECT COUNT(e) FROM Event e WHERE e.location.id = :locationId AND e.name = :name AND e.date < :now")
    long countOccurrencesByName(
            @Param("locationId") Long locationId,
            @Param("name") String name,
            @Param("now") LocalDateTime now);
}
