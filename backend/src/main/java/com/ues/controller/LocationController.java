package com.ues.controller;

import com.ues.dto.LocationDTO;
import com.ues.model.User;
import com.ues.repository.UserRepository;
import com.ues.security.UserDetailsImpl;
import com.ues.service.LocationService;
import com.ues.service.ReviewService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/locations")
public class LocationController {

    private static final Logger logger = LogManager.getLogger(LocationController.class);

    private final LocationService locationService;
    private final ReviewService reviewService;
    private final UserRepository userRepository;

    public LocationController(LocationService locationService, ReviewService reviewService,
                               UserRepository userRepository) {
        this.locationService = locationService;
        this.reviewService = reviewService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<List<LocationDTO>> searchLocations(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String type) {
        return ResponseEntity.ok(locationService.searchLocations(name, address, type));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LocationDTO> getLocation(@PathVariable Long id) {
        return ResponseEntity.ok(locationService.getLocation(id));
    }

    @GetMapping("/popular")
    public ResponseEntity<List<LocationDTO>> getPopularLocations() {
        return ResponseEntity.ok(locationService.getPopularLocations());
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LocationDTO> createLocation(
            @RequestParam String name,
            @RequestParam String address,
            @RequestParam String type,
            @RequestParam String description,
            @RequestParam MultipartFile image) {
        logger.info("Creating location: {}", name);
        return ResponseEntity.ok(locationService.createLocation(name, address, type, description, image));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<LocationDTO> updateLocation(
            @PathVariable Long id,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) MultipartFile image,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        // Managers can only update their own locations
        logger.info("Updating location: {}", id);
        return ResponseEntity.ok(locationService.updateLocation(id, address, type, description, image));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteLocation(@PathVariable Long id) {
        logger.info("Deleting location: {}", id);
        locationService.deleteLocation(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/reviews")
    public ResponseEntity<?> getReviews(
            @PathVariable Long id,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDir) {
        return ResponseEntity.ok(reviewService.getReviewsByLocation(id, sortBy, sortDir));
    }
}
