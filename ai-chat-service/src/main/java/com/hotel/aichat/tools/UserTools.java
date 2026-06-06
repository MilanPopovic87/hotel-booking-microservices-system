package com.hotel.aichat.tools;

import com.hotel.aichat.client.UserClient;
import com.hotel.aichat.dto.UserResponse;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserTools {

    private final UserClient userClient;

    public UserTools(UserClient userClient) {
        this.userClient = userClient;
    }

    @Tool(description = "Returns all registered users in the hotel system")
    public List<UserResponse> getAllUsers() {
        return userClient.getAllUsers();
    }

    @Tool(description = "Returns a user by their unique user id")
    public UserResponse getUserById(Long userId) {
        return userClient.getUserById(userId);
    }
}
