package com.hotel.userservice.dto;

import com.hotel.userservice.entity.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(

        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 30)
        @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can contain only letters, numbers and underscores")
        String username,

        @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
        String password,

        Role role

) {
}
