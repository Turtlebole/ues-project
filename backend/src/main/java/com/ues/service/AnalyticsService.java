package com.ues.service;

import com.ues.dto.AnalyticsDTO;
import com.ues.dto.EventDTO;
import com.ues.dto.LocationDTO;
import com.ues.dto.ReviewDTO;
import com.ues.model.Event;
import com.ues.model.Location;
import com.ues.model.Review;
import com.ues.repository.EventRepository;
import com.ues.repository.LocationRepository;
import com.ues.repository.ReviewRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private final EventRepository eventRepository;
    private final LocationRepository locationRepository;
    private final ReviewRepository reviewRepository;
    private final EventService eventService;
    private final LocationService locationService;
    private final ReviewService reviewService;

    public AnalyticsService(EventRepository eventRepository, LocationRepository locationRepository,
                             ReviewRepository reviewRepository, EventService eventService,
                             LocationService locationService, ReviewService reviewService) {
        this.eventRepository = eventRepository;
        this.locationRepository = locationRepository;
        this.reviewRepository = reviewRepository;
        this.eventService = eventService;
        this.locationService = locationService;
        this.reviewService = reviewService;
    }

    public AnalyticsDTO getAnalytics(Long locationId, LocalDateTime start, LocalDateTime end) {
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new RuntimeException("Location not found"));

        List<Event> events = eventRepository.findByLocationIdAndDateBetween(locationId, start, end);

        AnalyticsDTO dto = new AnalyticsDTO();
        dto.setTotalEvents(events.size());
        dto.setRegularEvents(events.stream().filter(Event::isRegular).count());
        dto.setIrregularEvents(events.stream().filter(e -> !e.isRegular()).count());
        dto.setFreeEvents(events.stream().filter(Event::isFree).count());
        dto.setPaidEvents(events.stream().filter(e -> !e.isFree()).count());

        List<EventDTO> eventDTOs = events.stream().map(eventService::toDTO).collect(Collectors.toList());

        List<Location> allLocations = locationRepository.findAll();
        List<LocationDTO> topLocations = allLocations.stream()
                .map(locationService::toDTO)
                .filter(l -> l.getAverageRating() != null)
                .sorted(Comparator.comparingDouble(LocationDTO::getAverageRating).reversed())
                .limit(5)
                .collect(Collectors.toList());
        dto.setTopRatedLocations(topLocations);

        List<Location> sortedByRating = allLocations.stream()
                .sorted((a, b) -> {
                    Double rA = reviewRepository.calculateAverageRatingForLocation(a);
                    Double rB = reviewRepository.calculateAverageRatingForLocation(b);
                    return Double.compare(rB != null ? rB : 0, rA != null ? rA : 0);
                })
                .collect(Collectors.toList());

        if (!sortedByRating.isEmpty()) {
            List<Review> recentReviews = reviewRepository.findTop3ByLocationOrderByCreatedAtDesc(sortedByRating.get(0));
            dto.setRecentReviews(recentReviews.stream().map(reviewService::toDTO).collect(Collectors.toList()));
        }

        return dto;
    }
}
