package com.ues.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AccountRequestDTO {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String username;
    private String status;
    private LocalDateTime requestDate;
}
