package com.dhbw.broker.bff.service;

import com.dhbw.broker.bff.dto.JwtAuthResponse;
import com.dhbw.broker.bff.dto.SignInInput;
import com.dhbw.broker.bff.dto.SignUpInput;
import com.dhbw.broker.bff.dto.UserDto;
import com.dhbw.broker.bff.domain.User;
import com.dhbw.broker.bff.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class IdentityServiceImpl implements IdentityService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public UserDto signUp(SignUpInput input) {
        final String email = normalize(input.getEmail());
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("E-Mail bereits vergeben");
        }

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail(email);
        user.setFirstName(input.getFirstName());
        user.setLastName(input.getLastName());
        user.setAdmin(false);
        user.setHashedPassword(passwordEncoder.encode(input.getPassword()));

        user = userRepository.save(user);
        return toDto(user);
    }

    @Override
    public JwtAuthResponse signIn(SignInInput input) {
        final String email = normalize(input.getEmail());
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Ungültige Zugangsdaten"));

        if (!passwordEncoder.matches(input.getPassword(), user.getHashedPassword())) {
            throw new IllegalArgumentException("Ungültige Zugangsdaten");
        }

        String token = jwtService.createAccessToken(user);
        Instant expiresAt = jwtService.getExpiresAt();

        JwtAuthResponse resp = new JwtAuthResponse();
        resp.setToken(token);
        resp.setTokenType("Bearer");
        resp.setExpiresAt(expiresAt);
        resp.setUser(toDto(user));
        return resp;
    }

    private static String normalize(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    private static UserDto toDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId() != null ? user.getId().toString() : null);
        dto.setEmail((String) user.getEmail());
        dto.setFirstName((String) user.getFirstName());
        dto.setLastName((String) user.getLastName());
        dto.setAdmin(user.isAdmin());
        return dto;
    }
}