package com.ues.dto;

import lombok.Data;

import java.util.List;

@Data
public class AnalyticsDTO {
    private long totalEvents;
    private long regularEvents;
    private long irregularEvents;
    private long freeEvents;
    private long paidEvents;
    private List<EventDTO> topRatedEvents;
    private List<EventDTO> lowestRatedEvents;
    private List<LocationDTO> topRatedLocations;
    private List<ReviewDTO> recentReviews;
}
