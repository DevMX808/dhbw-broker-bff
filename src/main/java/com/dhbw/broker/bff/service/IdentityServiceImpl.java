package com.dhbw.broker.bff.service;

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
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class IdentityServiceImpl implements IdentityService {

    private final UserRepository users;
    private final WalletAccountRepository walletAccounts;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public IdentityServiceImpl(UserRepository users,
                               WalletAccountRepository walletAccounts,
                               PasswordEncoder passwordEncoder,
                               JwtService jwtService) {
        this.users = users;
        this.walletAccounts = walletAccounts;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
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

        String jwt = jwtService.createAccessToken(saved);
        Instant exp = jwtService.getExpiresAt();
        return new JwtAuthResponse(jwt, "Bearer", exp);
    }

    @Override
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
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not active");
        }

        if (!passwordEncoder.matches(input.password(), u.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        String jwt = jwtService.createAccessToken(u);
        Instant exp = jwtService.getExpiresAt();
        return new JwtAuthResponse(jwt, "Bearer", exp);
    }
}