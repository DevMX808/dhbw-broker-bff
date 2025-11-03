package com.dhbw.broker.bff.dto;

public record ErrorResponse(
        String error,
        String message,
        int status
) {
    public static ErrorResponse of(String message, int status) {
        return new ErrorResponse("BAD_REQUEST", message, status);
    }

    public static ErrorResponse of(String error, String message, int status) {
        return new ErrorResponse(error, message, status);
    }
}