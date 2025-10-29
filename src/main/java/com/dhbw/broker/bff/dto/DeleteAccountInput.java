package com.dhbw.broker.bff.dto;

import jakarta.validation.constraints.NotBlank;

public record DeleteAccountInput(
    @NotBlank(message = "Passwort zur Bestätigung der Kontolöschung erforderlich")
    String password
) {}