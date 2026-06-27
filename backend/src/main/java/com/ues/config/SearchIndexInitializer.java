package com.ues.config;

import com.ues.service.LocationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Re-indexes existing locations into Elasticsearch once the app is ready, so search
 * works even for data created before Elasticsearch was introduced. If Elasticsearch
 * is unavailable the failure is logged and the application keeps running.
 */
@Component
public class SearchIndexInitializer {

    private static final Logger log = LoggerFactory.getLogger(SearchIndexInitializer.class);

    private final LocationService locationService;

    public SearchIndexInitializer(LocationService locationService) {
        this.locationService = locationService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void reindex() {
        try {
            locationService.reindexAll();
            log.info("Locations re-indexed in Elasticsearch.");
        } catch (Exception e) {
            log.warn("Could not re-index locations on startup: {}", e.getMessage());
        }
    }
}
