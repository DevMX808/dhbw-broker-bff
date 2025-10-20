package com.dhbw.broker.bff.dto;

import java.time.Instant;

public record JwtAuthResponse(
        String accessToken,
        String tokenType,
        Instant expiresAt
) {}