package com.dhbw.broker.bff.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordInput(
    @NotBlank(message = "Aktuelles Passwort ist erforderlich")
    String currentPassword,
    
    @NotBlank(message = "Neues Passwort ist erforderlich")
    @Size(min = 8, message = "Neues Passwort muss mindestens 8 Zeichen haben")
    String newPassword
) {}