package com.ues.dto;

import lombok.Data;

import java.util.List;

@Data
public class LocationDTO {
    private Long id;
    private String name;
    private String address;
    private String type;
    private String description;
    private String image;
    private String pdfDocument;
    private Double averageRating;
    private List<EventDTO> upcomingEvents;
    private List<UserDTO> managers;
}
