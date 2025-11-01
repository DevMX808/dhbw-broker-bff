package com.dhbw.broker.bff.controller;

import com.dhbw.broker.bff.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
        return ResponseEntity.ok(users);
    }
}