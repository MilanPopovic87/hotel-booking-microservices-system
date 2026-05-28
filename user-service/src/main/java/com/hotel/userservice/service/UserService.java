package com.hotel.userservice.service;

import com.hotel.userservice.client.BookingClient;
import com.hotel.userservice.dto.UpdateUserRequest;
import com.hotel.userservice.entity.User;
import com.hotel.userservice.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BookingClient bookingClient;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       BookingClient bookingClient,
                       PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.bookingClient = bookingClient;
        this.passwordEncoder = passwordEncoder;
    }

    // =========================
    // GET ALL USERS
    // =========================

    @PreAuthorize("hasRole('ADMIN')")
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
    public User updateUser(Long id, UpdateUserRequest request) {

        // 1. Check existing user
        User dbUser = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));

        // 2. Check username uniqueness
        userRepository.findByUsername(request.username())
                .ifPresent(existingUser -> {

                    if (!existingUser.getId().equals(id)) {
                        throw new ResponseStatusException(
                                HttpStatus.CONFLICT,
                                "Username already exists"
                        );
                    }
                });

        // 3. Update username
        dbUser.setUsername(request.username());

        // 4. Update role
        dbUser.setRole(request.role());

        // 5. Update password only if provided
        if (request.password() != null &&
                !request.password().isBlank()) {

            dbUser.setPassword(
                    passwordEncoder.encode(request.password())
            );
        }

        // If password is null/blank -> keep old password

        // 6. Save updated user
        return userRepository.save(dbUser);
    }

    // =========================
    // DELETE USER
    // =========================

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(Long id) {

        // 1. Check if user exists
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));

        // 2. Check bookings via Booking Service
        Boolean hasBookings = bookingClient.hasBookings(id);

        if (Boolean.TRUE.equals(hasBookings)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "User has bookings and cannot be deleted"
            );
        }

        // 3. Delete user
        userRepository.delete(user);
    }
}

