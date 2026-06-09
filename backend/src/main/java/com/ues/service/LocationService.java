package com.ues.service;

import com.ues.dto.LocationDTO;
import com.ues.dto.ReviewDTO;
import com.ues.dto.UserDTO;
import com.ues.model.*;
import com.ues.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LocationService {

    private final LocationRepository locationRepository;
    private final ManagesRepository managesRepository;
    private final ReviewRepository reviewRepository;
    private final EventRepository eventRepository;
    private final FileStorageService fileStorageService;

    public LocationService(LocationRepository locationRepository,
                           ManagesRepository managesRepository,
                           ReviewRepository reviewRepository,
                           EventRepository eventRepository,
                           FileStorageService fileStorageService) {
        this.locationRepository = locationRepository;
        this.managesRepository = managesRepository;
        this.reviewRepository = reviewRepository;
        this.eventRepository = eventRepository;
        this.fileStorageService = fileStorageService;
    }

    public List<LocationDTO> searchLocations(String name, String address, String type) {
        List<Location> locations = locationRepository.searchLocations(
            name != null ? name : "",
            address != null ? address : "",
            type != null ? type : ""
        );
        return locations.stream().map(this::toDTO).collect(Collectors.toList());
    }

    public LocationDTO getLocation(Long id) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Location not found"));
        return toDTOWithDetails(location);
    }

    @Transactional
    public LocationDTO createLocation(String name, String address, String type,
                                       String description, MultipartFile image) {
        String imageName = fileStorageService.storeFile(image);
        Location location = Location.builder()
                .name(name)
                .address(address)
                .type(type)
                .description(description)
                .image(imageName)
                .build();
        location = locationRepository.save(location);
        return toDTO(location);
    }

    @Transactional
    public LocationDTO updateLocation(Long id, String address, String type,
                                       String description, MultipartFile image) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Location not found"));
        if (address != null) location.setAddress(address);
        if (type != null) location.setType(type);
        if (description != null) location.setDescription(description);
        if (image != null && !image.isEmpty()) {
            fileStorageService.deleteFile(location.getImage());
            location.setImage(fileStorageService.storeFile(image));
        }
        location = locationRepository.save(location);
        return toDTO(location);
    }

    @Transactional
    public void deleteLocation(Long id) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Location not found"));
        fileStorageService.deleteFile(location.getImage());
        locationRepository.delete(location);
    }

    public List<LocationDTO> getPopularLocations() {
        List<Location> all = locationRepository.findAll();
        return all.stream()
                .map(l -> {
                    LocationDTO dto = toDTO(l);
                    return dto;
                })
                .filter(dto -> dto.getAverageRating() != null && dto.getAverageRating() > 0)
                .sorted(Comparator.comparingDouble(LocationDTO::getAverageRating).reversed())
                .limit(5)
                .collect(Collectors.toList());
    }

    public double calculateAverageRating(Location location) {
        List<Review> reviews = reviewRepository.findByLocationNotDeleted(location);
        if (reviews.isEmpty()) return 0.0;

        double total = 0.0;
        int count = 0;
        for (Review review : reviews) {
            if (review.getRate() != null) {
                Rate rate = review.getRate();
                List<Integer> ratings = new ArrayList<>();
                if (rate.getPerformanceRating() != null) ratings.add(rate.getPerformanceRating());
                if (rate.getSoundLightRating() != null) ratings.add(rate.getSoundLightRating());
                if (rate.getSpaceRating() != null) ratings.add(rate.getSpaceRating());
                if (rate.getOverallRating() != null) ratings.add(rate.getOverallRating());
                if (!ratings.isEmpty()) {
                    total += ratings.stream().mapToInt(Integer::intValue).average().orElse(0);
                    count++;
                }
            }
        }
        return count > 0 ? total / count : 0.0;
    }

    public LocationDTO toDTO(Location location) {
        LocationDTO dto = new LocationDTO();
        dto.setId(location.getId());
        dto.setName(location.getName());
        dto.setAddress(location.getAddress());
        dto.setType(location.getType());
        dto.setDescription(location.getDescription());
        dto.setImage(location.getImage());
        dto.setAverageRating(calculateAverageRating(location));

        List<Manages> managers = managesRepository.findByLocation(location);
        dto.setManagers(managers.stream().map(m -> {
            UserDTO udto = new UserDTO();
            udto.setId(m.getUser().getId());
            udto.setEmail(m.getUser().getEmail());
            udto.setFirstName(m.getUser().getFirstName());
            udto.setLastName(m.getUser().getLastName());
            udto.setUsername(m.getUser().getUsername());
            return udto;
        }).collect(Collectors.toList()));

        return dto;
    }

    private LocationDTO toDTOWithDetails(Location location) {
        LocationDTO dto = toDTO(location);
        LocalDateTime now = LocalDateTime.now();
        List<Event> upcomingEvents = eventRepository.findByLocation(location).stream()
                .filter(e -> e.getDate().isAfter(now) || e.getDate().isEqual(now))
                .collect(Collectors.toList());
        dto.setUpcomingEvents(upcomingEvents.stream().map(e -> {
            com.ues.dto.EventDTO edto = new com.ues.dto.EventDTO();
            edto.setId(e.getId());
            edto.setName(e.getName());
            edto.setLocationId(location.getId());
            edto.setLocationName(location.getName());
            edto.setAddress(e.getAddress());
            edto.setType(e.getType());
            edto.setDate(e.getDate());
            edto.setRegular(e.isRegular());
            edto.setPrice(e.getPrice());
            edto.setFree(e.isFree());
            edto.setImage(e.getImage());
            return edto;
        }).collect(Collectors.toList()));
        return dto;
    }
}
