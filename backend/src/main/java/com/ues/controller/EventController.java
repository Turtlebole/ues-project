package com.ues.controller;

import com.ues.dto.EventDTO;
import com.ues.service.EventService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private static final Logger logger = LogManager.getLogger(EventController.class);

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    public ResponseEntity<List<EventDTO>> getEvents(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Long locationId,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) Double maxPrice) {
        return ResponseEntity.ok(eventService.searchEvents(date, type, locationId, address, maxPrice));
    }

    @GetMapping("/today")
    public ResponseEntity<List<EventDTO>> getTodayEvents() {
        return ResponseEntity.ok(eventService.getTodayEvents());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventDTO> getEvent(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.getEvent(id));
    }

    @GetMapping("/location/{locationId}")
    public ResponseEntity<List<EventDTO>> getEventsByLocation(@PathVariable Long locationId) {
        return ResponseEntity.ok(eventService.getEventsByLocation(locationId));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<EventDTO> createEvent(
            @RequestParam Long locationId,
            @RequestParam String name,
            @RequestParam String address,
            @RequestParam String type,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date,
            @RequestParam boolean regular,
            @RequestParam(required = false) Double price,
            @RequestParam boolean free,
            @RequestParam MultipartFile image) {
        logger.info("Creating event: {} at location {}", name, locationId);
        return ResponseEntity.ok(eventService.createEvent(locationId, name, address, type,
                date, regular, price, free, image));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<EventDTO> updateEvent(
            @PathVariable Long id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date,
            @RequestParam(defaultValue = "false") boolean regular,
            @RequestParam(required = false) Double price,
            @RequestParam(defaultValue = "false") boolean free,
            @RequestParam(required = false) MultipartFile image) {
        logger.info("Updating event: {}", id);
        return ResponseEntity.ok(eventService.updateEvent(id, name, address, type, date, regular, price, free, image));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        logger.info("Deleting event: {}", id);
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }
}
