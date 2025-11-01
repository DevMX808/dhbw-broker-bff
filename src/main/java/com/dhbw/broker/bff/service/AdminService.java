package com.dhbw.broker.bff.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.dhbw.broker.bff.domain.Status;
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

    @Transactional
    public UserWithBalanceDto updateUserStatus(UUID userId, String newStatus, String currentUserEmail) {
        Status status;
        try {
            status = Status.valueOf(newStatus);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid status. Must be ACTIVATED or DEACTIVATED"
            );
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found with id: " + userId
                ));

        if (user.getEmail().equals(currentUserEmail) && status == Status.DEACTIVATED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "You cannot deactivate your own account"
            );
        }

        user.setStatus(status);
        User savedUser = userRepository.save(user);

        return mapToDto(savedUser);
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

    @Setter
    @Getter
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

    }
}