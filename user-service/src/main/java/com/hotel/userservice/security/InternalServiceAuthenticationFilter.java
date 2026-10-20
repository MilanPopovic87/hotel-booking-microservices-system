package com.hotel.userservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
public class InternalServiceAuthenticationFilter extends OncePerRequestFilter {

    private static final String INTERNAL_SERVICE_TOKEN =
            "X-Internal-Service-Token";

    private final String expectedServiceToken;

    public InternalServiceAuthenticationFilter(
            @Value("${internal.service-token:}") String expectedServiceToken) {

        this.expectedServiceToken = expectedServiceToken;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        if (!request.getRequestURI()
                .startsWith("/api/users/internal/ai/")) {

            filterChain.doFilter(request, response);
            return;
        }

        if (expectedServiceToken.isBlank()) {
            response.setStatus(
                    HttpServletResponse.SC_SERVICE_UNAVAILABLE
            );
            return;
        }

        String providedToken =
                request.getHeader(INTERNAL_SERVICE_TOKEN);

        if (!isValidToken(providedToken)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isValidToken(String providedToken) {

        if (providedToken == null || providedToken.isBlank()) {
            return false;
        }

        return MessageDigest.isEqual(
                providedToken.getBytes(StandardCharsets.UTF_8),
                expectedServiceToken.getBytes(StandardCharsets.UTF_8)
        );
    }
}
