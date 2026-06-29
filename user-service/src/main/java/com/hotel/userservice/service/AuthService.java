package com.hotel.userservice.service;

import com.hotel.userservice.dto.*;
import com.hotel.userservice.entity.Role;
import com.hotel.userservice.entity.User;
import com.hotel.userservice.kafka.AuditEventProducer;
import com.hotel.userservice.repository.UserRepository;
import com.hotel.userservice.security.CustomUserPrincipal;
import com.hotel.userservice.security.JwtService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuditEventProducer auditEventProducer;
    private static final Logger log =
            LoggerFactory.getLogger(AuthService.class);

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuditEventProducer auditEventProducer) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.auditEventProducer = auditEventProducer;
    }

    @PostConstruct
    public void initAdminUser() {

        userRepository.findByUsername("admin")
                .orElseGet(() -> {

                    User admin = new User();

                    admin.setUsername("admin");
                    admin.setPassword(passwordEncoder.encode("admin123"));
                    admin.setRole(Role.ADMIN);

                    return userRepository.save(admin);
                });
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void register(RegisterRequest request, Authentication authentication) {

        userRepository.findByUsername(request.username())
                .ifPresent(user -> {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Username already exists"
                    );
                });

        User newUser = new User();

        newUser.setUsername(request.username());
        newUser.setPassword(passwordEncoder.encode(request.password()));
        newUser.setRole(Role.USER);

        User savedUser = userRepository.save(newUser);

        // Send audit event
        CustomUserPrincipal admin =
                (CustomUserPrincipal) authentication.getPrincipal();

        Map<String, Object> payload = Map.of(
                "username", savedUser.getUsername(),
                "role", savedUser.getRole().name()
        );

        AuditEventRequest auditEvent = new AuditEventRequest(
                UUID.randomUUID(),
                AuditEventType.USER_REGISTERED,
                "user-service",
                admin.getUsername(),
                "USER",
                savedUser.getId(),
                payload,
                "User registered successfully"
        );

        try {
            auditEventProducer.send(auditEvent);
        } catch (Exception e) {
            log.error("Failed to send audit event for user {}", savedUser.getId(), e);
        }
    }

    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));

        boolean passwordMatches =
                passwordEncoder.matches(
                        request.password(),
                        user.getPassword()
                );

        if (!passwordMatches) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid credentials"
            );
        }

        String token = jwtService.generateToken(
                user.getId(),
                user.getUsername(),
                user.getRole().name()
        );

        return new AuthResponse(token);
    }
}
