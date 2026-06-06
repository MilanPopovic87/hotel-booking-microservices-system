package com.hotel.aichat.client;

import com.hotel.aichat.dto.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "user-service", url = "${services.user.url}")
public interface UserClient {

    @GetMapping("/api/users/internal/all")
    List<UserResponse> getAllUsers();

    @GetMapping("/api/users/internal/{id}")
    UserResponse getUserById(@PathVariable("id") Long id);
}
