package com.ues.controller;

import com.ues.dto.AccountRequestDTO;
import com.ues.dto.UserDTO;
import com.ues.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

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
    public ResponseEntity<Void> approveRequest(@PathVariable Long id) {
        adminService.approveRequest(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/requests/{id}/reject")
    public ResponseEntity<Void> rejectRequest(@PathVariable Long id) {
        adminService.rejectRequest(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @PostMapping("/locations/{locationId}/managers")
    public ResponseEntity<Void> addManager(
            @PathVariable Long locationId,
            @RequestBody Map<String, Long> body) {
        Long userId = body.get("userId");
        adminService.addManager(locationId, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/locations/{locationId}/managers/{userId}")
    public ResponseEntity<Void> removeManager(
            @PathVariable Long locationId,
            @PathVariable Long userId) {
        adminService.removeManager(locationId, userId);
        return ResponseEntity.ok().build();
    }
}
