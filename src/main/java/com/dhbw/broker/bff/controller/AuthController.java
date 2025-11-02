package com.dhbw.broker.bff.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dhbw.broker.bff.config.RateLimitConfig;
import com.dhbw.broker.bff.dto.JwtAuthResponse;
import com.dhbw.broker.bff.dto.SignInInput;
import com.dhbw.broker.bff.dto.SignUpInput;
import com.dhbw.broker.bff.util.TooManyRequestsException;
import com.dhbw.broker.bff.service.IdentityService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping(
        value = "/auth",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
)
public class AuthController {

    private final IdentityService identityService;
    private final RateLimitConfig.RateLimitService rateLimitService;

    public AuthController(IdentityService identityService, 
                         RateLimitConfig.RateLimitService rateLimitService) {
        this.identityService = identityService;
        this.rateLimitService = rateLimitService;
    }

    @PostMapping("/register")
    public ResponseEntity<JwtAuthResponse> register(@Valid @RequestBody SignUpInput input,
                                                   HttpServletRequest request) {
        String clientIp = getClientIpAddress(request);
        
        if (!rateLimitService.isRegisterAllowed(clientIp)) {
            throw new TooManyRequestsException(
                "Too many registration attempts. Please try again later.");
        }
        
        JwtAuthResponse res = identityService.register(input);
        return ResponseEntity
                .ok()
                .header("Authorization", res.tokenType() + " " + res.accessToken())
                .body(res);
    }

    @PostMapping("/login")
    public ResponseEntity<JwtAuthResponse> login(@Valid @RequestBody SignInInput input,
                                               HttpServletRequest request) {
        String clientIp = getClientIpAddress(request);
        
        if (!rateLimitService.isLoginAllowed(clientIp)) {
            throw new TooManyRequestsException(
                "Too many login attempts. Please try again later.");
        }
        
        JwtAuthResponse res = identityService.login(input);
        return ResponseEntity
                .ok()
                .header("Authorization", res.tokenType() + " " + res.accessToken())
                .body(res);
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}