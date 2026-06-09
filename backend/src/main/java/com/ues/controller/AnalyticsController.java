package com.ues.controller;

import com.ues.dto.AnalyticsDTO;
import com.ues.service.AnalyticsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@RestController
@RequestMapping("/api/analytics")
@PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/locations/{locationId}")
    public ResponseEntity<AnalyticsDTO> getAnalytics(
            @PathVariable Long locationId,
            @RequestParam(defaultValue = "monthly") String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        LocalDateTime start;
        LocalDateTime end = LocalDateTime.now();

        if (startDate != null && endDate != null) {
            start = startDate.atStartOfDay();
            end = endDate.atTime(LocalTime.MAX);
        } else {
            start = switch (period) {
                case "weekly" -> LocalDate.now().minusWeeks(1).atStartOfDay();
                case "yearly" -> LocalDate.now().minusYears(1).atStartOfDay();
                default -> LocalDate.now().minusMonths(1).atStartOfDay();
            };
        }

        return ResponseEntity.ok(analyticsService.getAnalytics(locationId, start, end));
    }
}
