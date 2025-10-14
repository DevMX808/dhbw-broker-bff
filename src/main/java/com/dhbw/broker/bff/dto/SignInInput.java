package com.dhbw.broker.bff.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SignInInput {
    @NotBlank @Email private String email;
    @NotBlank @Size(min = 8, max = 255) private String password;
}