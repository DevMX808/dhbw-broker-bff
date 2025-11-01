package com.dhbw.broker.bff.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.dhbw.broker.bff.domain.User;
import com.dhbw.broker.bff.repository.UserRepository;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final WalletService walletService;

    public AdminService(UserRepository userRepository, WalletService walletService) {
        this.userRepository = userRepository;
        this.walletService = walletService;
    }

    public List<UserWithBalanceDto> getAllUsersWithBalances() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private UserWithBalanceDto mapToDto(User user) {
        BigDecimal balance = walletService.getCurrentBalance(user.getUserId());
        UserWithBalanceDto dto = new UserWithBalanceDto();
        dto.setUserId(user.getUserId());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setRole(user.getRole() != null ? user.getRole().name() : null);
        dto.setStatus(user.getStatus() != null ? user.getStatus().name() : null);
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        dto.setBalance(balance);
        return dto;
    }

    public static class UserWithBalanceDto {
        private java.util.UUID userId;
        private String email;
        private String firstName;
        private String lastName;
        private String role;
        private String status;
        private java.time.OffsetDateTime createdAt;
        private java.time.OffsetDateTime updatedAt;
        private BigDecimal balance;

        // Getters and Setters
        public java.util.UUID getUserId() { return userId; }
        public void setUserId(java.util.UUID userId) { this.userId = userId; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public java.time.OffsetDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(java.time.OffsetDateTime createdAt) { this.createdAt = createdAt; }
        public java.time.OffsetDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(java.time.OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
        public BigDecimal getBalance() { return balance; }
        public void setBalance(BigDecimal balance) { this.balance = balance; }
    }ehler beim Laden der Benutzer (Status: 200): Unexpected token '<', "<!doctype "... is not valid JSON
}