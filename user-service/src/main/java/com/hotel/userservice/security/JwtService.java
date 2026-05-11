package com.hotel.userservice.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expirationMinutes}")
    private int expirationMinutes;

    // ------------------------
    // SIGNING KEY
    // ------------------------
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    // ------------------------
    // GENERATE TOKEN
    // ------------------------
    public String generateToken(Long userId, String username, String role) {

        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMinutes * 60 * 1000L);

        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // ------------------------
    // EXTRACT CLAIMS (IMPORTANT)
    // ------------------------
    public Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // ------------------------
    // VALIDATE TOKEN
    // ------------------------
    public boolean validateToken(String token) {
        try {
            extractClaims(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

}
