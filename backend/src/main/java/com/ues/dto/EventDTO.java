package com.ues.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EventDTO {
    private Long id;
    private String name;
    private Long locationId;
    private String locationName;
    private String address;
    private String type;
    private LocalDateTime date;
    private boolean regular;
    private Double price;
    private boolean free;
    private String image;
    private long occurrenceCount;
}
