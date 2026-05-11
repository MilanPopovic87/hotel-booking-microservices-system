package com.hotel.userservice.controller;

import com.hotel.userservice.dto.UserResponse;
import com.hotel.userservice.entity.User;
import com.hotel.userservice.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // =========================
    // MAPPER
    // =========================
    private UserResponse mapToResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getRole()
        );
    }

    // =========================
    // GET ALL USERS
    // =========================
    @GetMapping
    public List<UserResponse> getAllUsers() {
        return userService.getAllUsers()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // =========================
    // GET USER BY ID
    // =========================
    @GetMapping("/{id}")
    public UserResponse getUserById(@PathVariable Long id) {
        return mapToResponse(userService.getUserById(id));
    }

    // =========================
    // GET USER BY USERNAME
    // =========================
    @GetMapping("/by-username/{username}")
    public UserResponse getUserByUsername(@PathVariable String username) {
        return mapToResponse(userService.getUserByUsername(username));
    }

    // =========================
    // UPDATE USER (ADMIN ONLY)
    // =========================
    @PutMapping("/{id}")
    public UserResponse updateUser(@PathVariable Long id,
                                   @RequestBody User user) {

        return mapToResponse(userService.updateUser(id, user));
    }

    // =========================
    // DELETE USER (ADMIN ONLY)
    // =========================
    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }
}
