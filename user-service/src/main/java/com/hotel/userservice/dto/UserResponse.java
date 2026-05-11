package com.hotel.userservice.dto;

import com.hotel.userservice.entity.Role;

public record UserResponse(
        Long id,
        String username,
        Role role
) {
}
