package com.dhbw.broker.bff.service;

import java.time.Instant;

public record AccessToken(String value, Instant expiresAt) {}