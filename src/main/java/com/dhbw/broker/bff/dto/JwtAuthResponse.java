package com.dhbw.broker.bff.dto;

import lombok.Data;
import java.time.Instant;

@Data
public class JwtAuthResponse {
    private String token;
    private String tokenType = "Bearer";
    private Instant expiresAt;
    private UserDto user;
}