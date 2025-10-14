package com.dhbw.broker.bff.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class UserDto {
    private String id;
    private String email;
    private String firstName;
    private String lastName;

    @JsonProperty("isAdmin")
    private boolean isAdmin;
}