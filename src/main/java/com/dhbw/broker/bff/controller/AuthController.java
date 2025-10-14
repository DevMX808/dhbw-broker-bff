package com.dhbw.broker.bff.controller;

import com.dhbw.broker.bff.dto.JwtAuthResponse;
import com.dhbw.broker.bff.dto.SignInInput;
import com.dhbw.broker.bff.dto.SignUpInput;
import com.dhbw.broker.bff.dto.UserDto;
import com.dhbw.broker.bff.service.IdentityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping ("/auth")
public class AuthController {

    private final IdentityService identityService;

    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@Valid @RequestBody SignUpInput input) {
        UserDto userDto = identityService.signUp(input);   // synchroner Service
        return ResponseEntity.status(HttpStatus.CREATED).body(userDto);
    }

    @PostMapping("/login")
    public ResponseEntity<JwtAuthResponse> login(@Valid @RequestBody SignInInput input) {
        JwtAuthResponse jwt = identityService.signIn(input);
        return ResponseEntity.ok(jwt);
    }


    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Void> handleBadCreds() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}