package com.ues.dto;

import lombok.Data;

import java.util.List;

@Data
public class UserDTO {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String username;
    private String profileImage;
    private String role;
    private List<ReviewDTO> reviews;
    private List<LocationDTO> managedLocations;
}
