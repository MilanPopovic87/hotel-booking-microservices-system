package com.hotel.apigateway.security;

import com.hotel.apigateway.config.JwtProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private static final String SECRET =
            "YourSuperSecretKeyAtLeast32CharsLong123!";

    private JwtService jwtService;

    private SecretKey signingKey;

    @BeforeEach
    void setUp() {

        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setSecret(SECRET);
        jwtProperties.setExpirationMinutes(60);

        jwtService = new JwtService(jwtProperties);

        signingKey = Keys.hmacShaKeyFor(
                SECRET.getBytes(StandardCharsets.UTF_8)
        );
    }

    @Test
    void shouldValidateValidToken() {

        String token = createToken(
                "admin",
                "ADMIN",
                null
        );

        assertTrue(jwtService.isValid(token));
    }

    @Test
    void shouldRejectInvalidToken() {

        String token = "invalid.jwt.token";

        assertFalse(jwtService.isValid(token));
    }

    @Test
    void shouldRejectExpiredToken() {

        String token = createToken(
                "admin",
                "ADMIN",
                new Date(System.currentTimeMillis() - 60_000)
        );

        assertFalse(jwtService.isValid(token));
    }

    @Test
    void shouldExtractClaimsFromValidToken() {

        String token = createToken(
                "admin",
                "ADMIN",
                null
        );

        var claims = jwtService.extractClaims(token);

        assertEquals("admin", claims.getSubject());
        assertEquals("ADMIN", claims.get("role", String.class));
    }

    private String createToken(
            String username,
            String role,
            Date expiration
    ) {

        var builder = Jwts.builder()
                .setSubject(username)
                .claim("role", role)
                .setIssuedAt(new Date());

        if (expiration != null) {
            builder.setExpiration(expiration);
        } else {
            builder.setExpiration(
                    new Date(System.currentTimeMillis() + 60_000)
            );
        }

        return builder
                .signWith(signingKey)
                .compact();
    }
}
