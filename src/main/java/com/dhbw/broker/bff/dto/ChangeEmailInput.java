package com.dhbw.broker.bff.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangeEmailInput(
    @NotBlank(message = "Neue E-Mail ist erforderlich")
    @Email(message = "Ungültige E-Mail-Adresse")
    @Size(max = 320, message = "E-Mail darf maximal 320 Zeichen haben")
    String newEmail,
    
    @NotBlank(message = "Passwort zur Bestätigung erforderlich")
    String password
) {}