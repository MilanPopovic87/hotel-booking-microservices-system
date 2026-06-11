package com.hotel.booking.client;

import com.hotel.booking.dto.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", url = "${services.user.url}")
public interface UserClient {

    @GetMapping("/api/users/internal/{id}")
    UserResponse getUserById(@PathVariable Long id);
}
