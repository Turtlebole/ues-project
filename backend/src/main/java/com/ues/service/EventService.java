package com.ues.service;

import com.ues.dto.EventDTO;
import com.ues.model.Event;
import com.ues.model.Location;
import com.ues.repository.EventRepository;
import com.ues.repository.LocationRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EventService {

    private static final Logger logger = LogManager.getLogger(EventService.class);

    private final EventRepository eventRepository;
    private final LocationRepository locationRepository;
    private final FileStorageService fileStorageService;

    public EventService(EventRepository eventRepository,
                        LocationRepository locationRepository,
                        FileStorageService fileStorageService) {
        this.eventRepository = eventRepository;
        this.locationRepository = locationRepository;
        this.fileStorageService = fileStorageService;
    }

    public List<EventDTO> getTodayEvents() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
        return eventRepository.findTodayEvents(startOfDay, endOfDay)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<EventDTO> searchEvents(LocalDate date, String type, Long locationId,
                                        String address, Double maxPrice) {
        LocalDateTime start = (date != null ? date : LocalDate.now()).atStartOfDay();
        LocalDateTime end = start.toLocalDate().atTime(LocalTime.MAX);
        return eventRepository.searchEvents(start, end, type, locationId, address, maxPrice)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<EventDTO> getEventsByLocation(Long locationId) {
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new RuntimeException("Location not found"));
        return eventRepository.findByLocation(location)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public EventDTO getEvent(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        return toDTO(event);
    }

    @Transactional
    public EventDTO createEvent(Long locationId, String name, String address, String type,
                                 LocalDateTime date, boolean regular, Double price,
                                 boolean free, MultipartFile image) {
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new RuntimeException("Location not found"));
        String imageName = fileStorageService.storeFile(image);
        Event event = Event.builder()
                .name(name)
                .location(location)
                .address(address)
                .type(type)
                .date(date)
                .regular(regular)
                .price(free ? null : price)
                .free(free)
                .image(imageName)
                .build();
        event = eventRepository.save(event);
        logger.info("Event created: {} at location {}", name, locationId);
        return toDTO(event);
    }

    @Transactional
    public EventDTO updateEvent(Long id, String name, String address, String type,
                                 LocalDateTime date, boolean regular, Double price,
                                 boolean free, MultipartFile image) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        if (name != null) event.setName(name);
        if (address != null) event.setAddress(address);
        if (type != null) event.setType(type);
        if (date != null) event.setDate(date);
        event.setRegular(regular);
        event.setFree(free);
        event.setPrice(free ? null : price);
        if (image != null && !image.isEmpty()) {
            fileStorageService.deleteFile(event.getImage());
            event.setImage(fileStorageService.storeFile(image));
        }
        event = eventRepository.save(event);
        logger.info("Event updated: {}", id);
        return toDTO(event);
    }

    @Transactional
    public void deleteEvent(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        fileStorageService.deleteFile(event.getImage());
        eventRepository.delete(event);
        logger.info("Event deleted: {}", id);
    }

    public EventDTO toDTO(Event event) {
        EventDTO dto = new EventDTO();
        dto.setId(event.getId());
        dto.setName(event.getName());
        dto.setLocationId(event.getLocation().getId());
        dto.setLocationName(event.getLocation().getName());
        dto.setAddress(event.getAddress());
        dto.setType(event.getType());
        dto.setDate(event.getDate());
        dto.setRegular(event.isRegular());
        dto.setPrice(event.getPrice());
        dto.setFree(event.isFree());
        dto.setImage(event.getImage());
        dto.setOccurrenceCount(eventRepository.countOccurrencesByName(
                event.getLocation().getId(), event.getName(), LocalDateTime.now()));
        return dto;
    }
}
