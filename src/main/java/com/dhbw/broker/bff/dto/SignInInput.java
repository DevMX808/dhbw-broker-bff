package com.dhbw.broker.bff.dto;

public record SignInInput(
        @jakarta.validation.constraints.Email String email,
        @jakarta.validation.constraints.NotBlank String password
) {}