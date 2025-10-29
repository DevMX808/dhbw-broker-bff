package com.dhbw.broker.bff.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProfileInput(
    @NotBlank(message = "Vorname ist erforderlich")
    @Size(max = 100, message = "Vorname darf maximal 100 Zeichen haben")
    String firstName,
    
    @NotBlank(message = "Nachname ist erforderlich")
    @Size(max = 100, message = "Nachname darf maximal 100 Zeichen haben")
    String lastName
) {}