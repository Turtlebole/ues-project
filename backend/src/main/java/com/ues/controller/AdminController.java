package com.ues.controller;

import com.ues.dto.AccountRequestDTO;
import com.ues.dto.UserDTO;
import com.ues.service.AdminService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private static final Logger logger = LogManager.getLogger(AdminController.class);

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/requests")
    public ResponseEntity<List<AccountRequestDTO>> getAllRequests() {
        return ResponseEntity.ok(adminService.getAllRequests());
    }

    @GetMapping("/requests/pending")
    public ResponseEntity<List<AccountRequestDTO>> getPendingRequests() {
        return ResponseEntity.ok(adminService.getPendingRequests());
    }

    @PostMapping("/requests/{id}/approve")
    public ResponseEntity<String> approveRequest(@PathVariable Long id) {
        logger.info("Approving registration request: {}", id);
        adminService.approveRequest(id);
        return ResponseEntity.ok("Request approved");
    }

    @PostMapping("/requests/{id}/reject")
    public ResponseEntity<String> rejectRequest(@PathVariable Long id) {
        logger.info("Rejecting registration request: {}", id);
        adminService.rejectRequest(id);
        return ResponseEntity.ok("Request rejected");
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @PostMapping("/locations/{locationId}/managers")
    public ResponseEntity<String> addManager(
            @PathVariable Long locationId,
            @RequestBody Map<String, Long> body) {
        Long userId = body.get("userId");
        logger.info("Adding manager {} to location {}", userId, locationId);
        adminService.addManager(locationId, userId);
        return ResponseEntity.ok("Manager added");
    }

    @DeleteMapping("/locations/{locationId}/managers/{userId}")
    public ResponseEntity<String> removeManager(
            @PathVariable Long locationId,
            @PathVariable Long userId) {
        logger.info("Removing manager {} from location {}", userId, locationId);
        adminService.removeManager(locationId, userId);
        return ResponseEntity.ok("Manager removed");
    }
}
