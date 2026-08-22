package com.hotel.aichat.security;

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
public class AiServiceAuthenticationFilter extends OncePerRequestFilter {

    private static final String INTERNAL_SERVICE_TOKEN =
            "X-Internal-Service-Token";

    private final String expectedServiceToken;

    public AiServiceAuthenticationFilter(
            @Value("${ai.service-token:}") String expectedServiceToken) {

        if (expectedServiceToken.isBlank()) {
            throw new IllegalStateException(
                    "AI_SERVICE_TOKEN must be configured for the AI Chat Service."
            );
        }

        this.expectedServiceToken = expectedServiceToken;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

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