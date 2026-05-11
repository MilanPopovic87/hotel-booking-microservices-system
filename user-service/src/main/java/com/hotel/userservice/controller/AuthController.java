package com.hotel.userservice.controller;

import com.hotel.userservice.dto.AuthResponse;
import com.hotel.userservice.dto.LoginRequest;
import com.hotel.userservice.dto.RegisterRequest;
import com.hotel.userservice.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // ------------------------
    // REGISTER
    // ------------------------
    @PostMapping("/register")
    public void register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
    }

    // ------------------------
    // LOGIN
    // ------------------------
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}