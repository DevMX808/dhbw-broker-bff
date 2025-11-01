package com.dhbw.broker.bff.controller;

import java.util.List;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.dhbw.broker.bff.service.AdminService;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/users-with-balances")
    public ResponseEntity<List<AdminService.UserWithBalanceDto>> getAllUsersWithBalances() {
        List<AdminService.UserWithBalanceDto> users = adminService.getAllUsersWithBalances();
        return ResponseEntity.ok()
                .header("Cache-Control", "no-cache, no-store, must-revalidate")
                .header("Pragma", "no-cache")
                .header("Expires", "0")
                .body(users);
    }

    @PutMapping("/users/{userId}/status")
    public ResponseEntity<AdminService.UserWithBalanceDto> updateUserStatus(
            @PathVariable UUID userId,
            @RequestBody StatusUpdateRequest request,
            Authentication authentication) {

        String currentUserEmail = authentication.getName();

        AdminService.UserWithBalanceDto updatedUser = adminService.updateUserStatus(
                userId,
                request.getStatus(),
                currentUserEmail
        );

        return ResponseEntity.ok(updatedUser);
    }

    @Setter
    @Getter
    public static class StatusUpdateRequest {
        private String status;

    }
}