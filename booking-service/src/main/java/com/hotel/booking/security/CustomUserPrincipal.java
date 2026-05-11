package com.hotel.booking.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class CustomUserPrincipal implements UserDetails {

    private final Long id;
    private final String username;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserPrincipal(
            Long id,
            String username,
            Collection<? extends GrantedAuthority> authorities
    ) {
        this.id = id;
        this.username = username;
        this.authorities = authorities;
    }

    public Long getId() {
        return id;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return username;
    }
}