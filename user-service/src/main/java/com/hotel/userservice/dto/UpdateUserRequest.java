package com.hotel.userservice.dto;

import com.hotel.userservice.entity.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(

        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 20)
        String username,

        String password,

        Role role

) {
}
