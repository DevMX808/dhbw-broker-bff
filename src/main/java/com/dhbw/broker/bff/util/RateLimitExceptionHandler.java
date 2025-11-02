package com.dhbw.broker.bff.util;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class RateLimitExceptionHandler {

    @ExceptionHandler(TooManyRequestsException.class)
    public ResponseEntity<Map<String, Object>> handleTooManyRequests(
            TooManyRequestsException ex, WebRequest request) {
        
        Map<String, Object> errorResponse = Map.of(
            "timestamp", LocalDateTime.now(),
            "status", HttpStatus.TOO_MANY_REQUESTS.value(),
            "error", "Too Many Requests",
            "message", ex.getMessage(),
            "path", request.getDescription(false).replace("uri=", "")
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.TOO_MANY_REQUESTS);
    }
}