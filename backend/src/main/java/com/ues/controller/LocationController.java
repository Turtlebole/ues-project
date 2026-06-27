package com.ues.controller;

import com.ues.dto.LocationDTO;
import com.ues.repository.UserRepository;
import com.ues.security.UserDetailsImpl;
import com.ues.service.LocationService;
import com.ues.service.ReviewService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
public class LocationController {

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
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String query) {
        return ResponseEntity.ok(locationService.searchLocations(name, address, type, query));
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
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<LocationDTO> createLocation(
            @RequestParam String name,
            @RequestParam String address,
            @RequestParam String type,
            @RequestParam String description,
            @RequestParam MultipartFile image,
            @RequestParam(required = false) MultipartFile pdf) {
        return ResponseEntity.ok(locationService.createLocation(name, address, type, description, image, pdf));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<LocationDTO> updateLocation(
            @PathVariable Long id,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) MultipartFile image,
            @RequestParam(required = false) MultipartFile pdf,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(locationService.updateLocation(id, address, type, description, image, pdf));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteLocation(@PathVariable Long id) {
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
