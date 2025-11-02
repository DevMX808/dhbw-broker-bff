package com.dhbw.broker.bff.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.dhbw.broker.bff.domain.Role;
import com.dhbw.broker.bff.domain.Status;
import com.dhbw.broker.bff.domain.User;
import com.dhbw.broker.bff.domain.WalletAccount;
import com.dhbw.broker.bff.dto.JwtAuthResponse;
import com.dhbw.broker.bff.dto.SignInInput;
import com.dhbw.broker.bff.dto.SignUpInput;
import com.dhbw.broker.bff.repository.UserRepository;
import com.dhbw.broker.bff.repository.WalletAccountRepository;

import jakarta.transaction.Transactional;

@Service
public class IdentityServiceImpl implements IdentityService {

    private final UserRepository users;
    private final WalletAccountRepository walletAccounts;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final WalletService walletService;

    public IdentityServiceImpl(UserRepository users,
                               WalletAccountRepository walletAccounts,
                               PasswordEncoder passwordEncoder,
                               JwtService jwtService,
                               WalletService walletService) {
        this.users = users;
        this.walletAccounts = walletAccounts;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.walletService = walletService;
    }

    @Override
    @Transactional
    public JwtAuthResponse register(SignUpInput input) {
        if (input == null ||
                input.email() == null || input.email().isBlank() ||
                input.password() == null || input.password().isBlank() ||
                input.firstName() == null || input.firstName().isBlank() ||
                input.lastName() == null || input.lastName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing required fields");
        }

        String email = input.email().trim().toLowerCase();
        if (users.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }

        User u = new User();
        u.setEmail(email);
        u.setPasswordHash(passwordEncoder.encode(input.password()));
        u.setFirstName(input.firstName().trim());
        u.setLastName(input.lastName().trim());
        u.setRole(Role.USER);
        u.setStatus(Status.ACTIVATED);

        User saved = users.save(u);

        WalletAccount wa = new WalletAccount();
        wa.setUserId(saved.getId());
        wa.setCurrency("USD");
        walletAccounts.save(wa);

        walletService.addInitialCredit(saved, new java.math.BigDecimal("10000.00"), "Startguthaben bei Registrierung");

        boolean isAdmin = saved.getRole() == Role.ADMIN;
        AccessToken token = jwtService.issueAccessToken(
                saved.getId(),
                saved.getEmail(),
                saved.getFirstName(),
                saved.getLastName(),
                isAdmin
        );

        return new JwtAuthResponse(token.value(), "Bearer", token.expiresAt());
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public JwtAuthResponse login(SignInInput input) {
        if (input == null ||
                input.email() == null || input.email().isBlank() ||
                input.password() == null || input.password().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing credentials");
        }

        String email = input.email().trim().toLowerCase();
        User u = users.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (u.getStatus() != Status.ACTIVATED) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Ihr Account wurde gesperrt. Bitte kontaktieren Sie den Administrator.");
        }

        if (!passwordEncoder.matches(input.password(), u.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        boolean isAdmin = u.getRole() == Role.ADMIN;
        AccessToken token = jwtService.issueAccessToken(
                u.getId(),
                u.getEmail(),
                u.getFirstName(),
                u.getLastName(),
                isAdmin
        );

        return new JwtAuthResponse(token.value(), "Bearer", token.expiresAt());
    }

    @Override
    public User getCurrentUser() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        return users.findById(java.util.UUID.fromString(userId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    @Override
    @Transactional
    public User updateProfile(com.dhbw.broker.bff.dto.UpdateProfileInput input) {
        User user = getCurrentUser();
        user.setFirstName(input.firstName().trim());
        user.setLastName(input.lastName().trim());
        return users.save(user);
    }

    @Override
    @Transactional
    public void changePassword(com.dhbw.broker.bff.dto.ChangePasswordInput input) {
        User user = getCurrentUser();

        if (!passwordEncoder.matches(input.currentPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Aktuelles Passwort ist falsch");
        }

        user.setPasswordHash(passwordEncoder.encode(input.newPassword()));
        users.save(user);
    }

    @Override
    @Transactional
    public void changeEmail(com.dhbw.broker.bff.dto.ChangeEmailInput input) {
        User user = getCurrentUser();

        if (!passwordEncoder.matches(input.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Passwort ist falsch");
        }

        if (users.findByEmail(input.newEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "E-Mail bereits vergeben");
        }

        user.setEmail(input.newEmail().trim().toLowerCase());
        users.save(user);
    }

    @Override
    @Transactional
    public void deleteAccount(com.dhbw.broker.bff.dto.DeleteAccountInput input) {
        User user = getCurrentUser();

        if (!passwordEncoder.matches(input.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Passwort ist falsch");
        }

        users.delete(user);
    }
}