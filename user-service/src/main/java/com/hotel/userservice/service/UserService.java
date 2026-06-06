package com.hotel.userservice.service;

import com.hotel.userservice.client.AuditClient;
import com.hotel.userservice.client.BookingClient;
import com.hotel.userservice.dto.AuditEventRequest;
import com.hotel.userservice.dto.AuditEventType;
import com.hotel.userservice.dto.UpdateUserRequest;
import com.hotel.userservice.entity.User;
import com.hotel.userservice.repository.UserRepository;
import com.hotel.userservice.security.CustomUserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BookingClient bookingClient;
    private final PasswordEncoder passwordEncoder;
    private final AuditClient auditClient;
    private static final Logger log =
            LoggerFactory.getLogger(UserService.class);

    public UserService(UserRepository userRepository,
                       BookingClient bookingClient,
                       PasswordEncoder passwordEncoder,
                       AuditClient auditClient) {

        this.userRepository = userRepository;
        this.bookingClient = bookingClient;
        this.passwordEncoder = passwordEncoder;
        this.auditClient = auditClient;
    }

    // =========================
    // GET ALL USERS
    // =========================

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // =========================
    // GET USER BY ID
    // =========================

    public User getUserById(Long id) {

        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));
    }

    // =========================
    // GET USER BY USERNAME
    // =========================

    public User getUserByUsername(String username) {

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));
    }

    // =========================
    // UPDATE USER
    // =========================

    @PreAuthorize("hasRole('ADMIN')")
    public User updateUser(Long id, UpdateUserRequest request, Authentication authentication) {

        //  Check existing user
        User dbUser = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));

        //  Check username uniqueness
        userRepository.findByUsername(request.username())
                .ifPresent(existingUser -> {
                    if (!existingUser.getId().equals(id)) {
                        throw new ResponseStatusException(
                                HttpStatus.CONFLICT,
                                "Username already exists"
                        );
                    }
                });

        //  Update fields
        dbUser.setUsername(request.username());
        dbUser.setRole(request.role());

        if (request.password() != null && !request.password().isBlank()) {
            dbUser.setPassword(passwordEncoder.encode(request.password()));
        }

        User updatedUser = userRepository.save(dbUser);

        // ================= AUDIT =================
        CustomUserPrincipal admin =
                (CustomUserPrincipal) authentication.getPrincipal();

        Map<String, Object> payload = Map.of(
                "username", updatedUser.getUsername(),
                "role", updatedUser.getRole().name()
        );

        AuditEventRequest auditEvent = new AuditEventRequest(
                UUID.randomUUID(),
                AuditEventType.USER_UPDATED,
                "user-service",
                admin.getUsername(),
                "USER",
                updatedUser.getId(),
                payload,
                "User updated successfully"
        );

        try {
            auditClient.sendAuditEvent(auditEvent);
        } catch (Exception e) {
            log.error("Failed to send audit event for user {}", updatedUser.getId(), e);
        }

        return updatedUser;
    }

    // =========================
    // DELETE USER
    // =========================

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(Long id, Authentication authentication) {

        //  Check if user exists
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));

        //  Check bookings via Booking Service
        Boolean hasBookings = bookingClient.hasBookings(id);

        if (Boolean.TRUE.equals(hasBookings)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "User has bookings and cannot be deleted"
            );
        }

        // ================= AUDIT (snapshot BEFORE delete) =================
        CustomUserPrincipal admin =
                (CustomUserPrincipal) authentication.getPrincipal();

        Map<String, Object> payload = Map.of(
                "username", user.getUsername(),
                "role", user.getRole().name()
        );

        //  Delete user
        userRepository.delete(user);

        AuditEventRequest auditEvent = new AuditEventRequest(
                UUID.randomUUID(),
                AuditEventType.USER_DELETED,
                "user-service",
                admin.getUsername(),
                "USER",
                user.getId(),
                payload,
                "User deleted successfully"
        );

        try {
            auditClient.sendAuditEvent(auditEvent);
        } catch (Exception e) {
            log.error("Failed to send audit event for user {}", user.getId(), e);
        }
    }
}

