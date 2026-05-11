package com.hotel.userservice.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // 1. No token → continue chain
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        // 2. Validate token
        if (!jwtService.validateToken(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Extract claims
        Claims claims = jwtService.extractClaims(token);

        String username = claims.getSubject();
        String role = claims.get("role", String.class);

        Long userId = claims.get("userId", Long.class);

        // 4. Convert role → Spring Security authority
        var authorities = List.of(
                new SimpleGrantedAuthority("ROLE_" + role)
        );

        // 5. Create Authentication object
        var authentication = new UsernamePasswordAuthenticationToken(
                new CustomUserPrincipal(userId, username),
                null,
                authorities
        );

        // 6. Set security context
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 7. Continue filter chain
        filterChain.doFilter(request, response);
    }
}
