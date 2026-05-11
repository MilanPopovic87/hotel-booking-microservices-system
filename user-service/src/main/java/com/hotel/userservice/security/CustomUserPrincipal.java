package com.hotel.userservice.security;

public class CustomUserPrincipal {

    private final Long id;
    private final String username;

    public CustomUserPrincipal(Long id, String username) {
        this.id = id;
        this.username = username;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }
}
