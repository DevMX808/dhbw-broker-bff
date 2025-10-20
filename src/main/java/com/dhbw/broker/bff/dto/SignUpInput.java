package com.dhbw.broker.bff.dto;

public record SignUpInput(
        @jakarta.validation.constraints.Email String email,
        @jakarta.validation.constraints.NotBlank String password,
        @jakarta.validation.constraints.NotBlank String firstName,
        @jakarta.validation.constraints.NotBlank String lastName
) {}