package com.ues.controller;

import com.ues.dto.ChangePasswordRequest;
import com.ues.dto.UserDTO;
import com.ues.security.UserDetailsImpl;
import com.ues.service.UserService;
import jakarta.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger logger = LogManager.getLogger(UserController.class);

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDTO> getProfile(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(userService.getProfile(userDetails.getId()));
    }

    @PutMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDTO> updateProfile(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) MultipartFile profileImage,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        logger.info("Updating profile for user: {}", userDetails.getId());
        return ResponseEntity.ok(userService.updateProfile(userDetails.getId(), firstName, lastName, profileImage));
    }

    @PostMapping("/me/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        logger.info("Password change request for user: {}", userDetails.getId());
        userService.changePassword(userDetails.getId(), request);
        return ResponseEntity.ok("Password changed successfully");
    }
}
