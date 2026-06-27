package com.ues.service;

import com.ues.dto.LocationDTO;
import com.ues.model.*;
import com.ues.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LocationService {

    private final LocationRepository locationRepository;
    private final ManagesRepository managesRepository;
    private final ReviewRepository reviewRepository;
    private final EventRepository eventRepository;
    private final MinioStorageService minioStorageService;
    private final PdfTextExtractorService pdfTextExtractorService;
    private final LocationSearchService locationSearchService;

    public LocationService(LocationRepository locationRepository,
                           ManagesRepository managesRepository,
                           ReviewRepository reviewRepository,
                           EventRepository eventRepository,
                           MinioStorageService minioStorageService,
                           PdfTextExtractorService pdfTextExtractorService,
                           LocationSearchService locationSearchService) {
        this.locationRepository = locationRepository;
        this.managesRepository = managesRepository;
        this.reviewRepository = reviewRepository;
        this.eventRepository = eventRepository;
        this.minioStorageService = minioStorageService;
        this.pdfTextExtractorService = pdfTextExtractorService;
        this.locationSearchService = locationSearchService;
    }

    /**
     * S1 - Search locations. When {@code query} is set, performs an Elasticsearch
     * full-text search across name, description and parsed PDF content. Otherwise
     * falls back to the relational name/address/type filter.
     */
    public List<LocationDTO> searchLocations(String name, String address, String type, String query) {
        if (query != null && !query.isBlank()) {
            List<Long> ids = locationSearchService.search(query.trim());
            // Preserve relevance order returned by Elasticsearch.
            Map<Long, Location> byId = locationRepository.findAllById(ids).stream()
                    .collect(Collectors.toMap(Location::getId, l -> l, (a, b) -> a, LinkedHashMap::new));
            return ids.stream()
                    .map(byId::get)
                    .filter(java.util.Objects::nonNull)
                    .map(this::toDTO)
                    .collect(Collectors.toList());
        }

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
                                       String description, MultipartFile image, MultipartFile pdf) {
        String imageName = minioStorageService.storeFile(image);

        String pdfName = null;
        String pdfContent = null;
        if (pdf != null && !pdf.isEmpty()) {
            pdfName = minioStorageService.storeFile(pdf);
            pdfContent = pdfTextExtractorService.extractText(pdf);
        }

        Location location = Location.builder()
                .name(name)
                .address(address)
                .type(type)
                .description(description)
                .image(imageName)
                .pdfDocument(pdfName)
                .build();
        location = locationRepository.save(location);

        locationSearchService.index(location, pdfContent);
        return toDTO(location);
    }

    @Transactional
    public LocationDTO updateLocation(Long id, String address, String type,
                                       String description, MultipartFile image, MultipartFile pdf) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Location not found"));
        if (address != null) location.setAddress(address);
        if (type != null) location.setType(type);
        if (description != null) location.setDescription(description);
        if (image != null && !image.isEmpty()) {
            minioStorageService.deleteFile(location.getImage());
            location.setImage(minioStorageService.storeFile(image));
        }

        // Keep the previously indexed PDF text unless a new PDF replaces it.
        String pdfContent = locationSearchService.getIndexedPdfContent(id);
        if (pdf != null && !pdf.isEmpty()) {
            minioStorageService.deleteFile(location.getPdfDocument());
            location.setPdfDocument(minioStorageService.storeFile(pdf));
            pdfContent = pdfTextExtractorService.extractText(pdf);
        }

        location = locationRepository.save(location);
        locationSearchService.index(location, pdfContent);
        return toDTO(location);
    }

    @Transactional
    public void deleteLocation(Long id) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Location not found"));
        minioStorageService.deleteFile(location.getImage());
        minioStorageService.deleteFile(location.getPdfDocument());
        locationRepository.delete(location);
        locationSearchService.delete(id);
    }

    /** Re-indexes every location into Elasticsearch (used on startup). */
    public void reindexAll() {
        for (Location location : locationRepository.findAll()) {
            String pdfContent = locationSearchService.getIndexedPdfContent(location.getId());
            locationSearchService.index(location, pdfContent);
        }
    }

    public List<LocationDTO> getPopularLocations() {
        List<Location> all = locationRepository.findAll();
        return all.stream()
                .map(this::toDTO)
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
        dto.setPdfDocument(location.getPdfDocument());
        dto.setAverageRating(calculateAverageRating(location));

        List<Manages> managers = managesRepository.findByLocation(location);
        dto.setManagers(managers.stream().map(m -> {
            com.ues.dto.UserDTO udto = new com.ues.dto.UserDTO();
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
