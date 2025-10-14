package com.dhbw.broker.bff.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class SignUpInput {
    @NotBlank @Email private String email;
    @NotBlank @Size(min = 8, max = 255) private String password;
    @NotBlank @Size(max = 120) private String firstName;
    @NotBlank @Size(max = 120) private String lastName;
    private boolean isAdmin = false;
}