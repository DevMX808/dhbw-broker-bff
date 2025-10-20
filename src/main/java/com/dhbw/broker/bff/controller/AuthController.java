package com.dhbw.broker.bff.controller;

import com.dhbw.broker.bff.dto.JwtAuthResponse;
import com.dhbw.broker.bff.dto.SignInInput;
import com.dhbw.broker.bff.dto.SignUpInput;
import com.dhbw.broker.bff.service.IdentityService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(
        value = "/auth",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
)
public class AuthController {

    private final IdentityService identityService;

    public AuthController(IdentityService identityService) {
        this.identityService = identityService;
    }

    @PostMapping("/register")
    public ResponseEntity<JwtAuthResponse> register(@Valid @RequestBody SignUpInput input) {
        JwtAuthResponse res = identityService.register(input);
        return ResponseEntity
                .ok()
                .header("Authorization", res.tokenType() + " " + res.accessToken())
                .body(res);
    }

    @PostMapping("/login")
    public ResponseEntity<JwtAuthResponse> login(@Valid @RequestBody SignInInput input) {
        JwtAuthResponse res = identityService.login(input);
        return ResponseEntity
                .ok()
                .header("Authorization", res.tokenType() + " " + res.accessToken())
                .body(res);
    }
}