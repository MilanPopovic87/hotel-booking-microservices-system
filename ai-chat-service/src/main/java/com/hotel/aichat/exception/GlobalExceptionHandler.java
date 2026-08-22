package com.hotel.aichat.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AiRateLimitException.class)
    public ResponseEntity<Map<String, String>> handleAiRateLimit(
            AiRateLimitException exception) {

        return ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .body(Map.of(
                        "error", "AI usage limit reached",
                        "message", exception.getMessage()
                ));
    }

    @ExceptionHandler(AiAuthenticationException.class)
    public ResponseEntity<Map<String, String>> handleAiAuthenticationException(
            AiAuthenticationException exception) {

        return ResponseEntity
                .status(HttpStatus.BAD_GATEWAY)
                .body(Map.of(
                        "error", "AI provider authentication failed",
                        "message", exception.getMessage()
                ));
    }
}
