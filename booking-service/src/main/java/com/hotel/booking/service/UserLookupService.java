package com.hotel.booking.service;

import com.hotel.booking.client.UserClient;
import com.hotel.booking.dto.UserResponse;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class UserLookupService {

    private final UserClient userClient;

    public UserLookupService(UserClient userClient) {
        this.userClient = userClient;
    }

    @Cacheable("users")
    public UserResponse getUserById(Long id) {
        return userClient.getUserById(id);
    }
}
