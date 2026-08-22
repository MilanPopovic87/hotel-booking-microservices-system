package com.hotel.apigateway.security;

import com.hotel.apigateway.config.JwtProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {

    private static final String SECRET =
            "YourSuperSecretKeyAtLeast32CharsLong123!";

    private JwtProperties jwtProperties;
    private JwtService jwtService;
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private SecretKey signingKey;

    @BeforeEach
    void setUp() {

        jwtProperties = new JwtProperties();
        jwtProperties.setSecret(SECRET);
        jwtProperties.setExpirationMinutes(60);


        jwtService = new JwtService(jwtProperties);

        jwtAuthenticationFilter =
                new JwtAuthenticationFilter(jwtService);

        signingKey = Keys.hmacShaKeyFor(
                SECRET.getBytes(StandardCharsets.UTF_8)
        );
    }

    @Test
    void shouldContinueWithoutAuthenticationWhenAuthorizationHeaderIsMissing() {

        var request = MockServerHttpRequest
                .get("/api/bookings")
                .build();

        var exchange = MockServerWebExchange.from(request);

        WebFilterChain chain = mock(WebFilterChain.class);

        when(chain.filter(exchange))
                .thenReturn(Mono.empty());

        StepVerifier.create(
                        jwtAuthenticationFilter.filter(exchange, chain)
                )
                .verifyComplete();

        verify(chain).filter(exchange);
    }

    @Test
    void shouldContinueWithoutAuthenticationWhenTokenIsInvalid() {

        var request = MockServerHttpRequest
                .get("/api/bookings")
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer invalid-token"
                )
                .build();

        var exchange = MockServerWebExchange.from(request);

        WebFilterChain chain = mock(WebFilterChain.class);

        when(chain.filter(exchange))
                .thenReturn(Mono.empty());

        StepVerifier.create(
                        jwtAuthenticationFilter.filter(exchange, chain)
                )
                .verifyComplete();

        verify(chain).filter(exchange);
    }

    @Test
    void shouldCreateAuthenticationWhenTokenIsValid() {

        String token = createToken(
                "admin",
                "ADMIN"
        );

        var request = MockServerHttpRequest
                .get("/api/chat")
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + token
                )
                .build();

        var exchange = MockServerWebExchange.from(request);

        WebFilterChain chain = mock(WebFilterChain.class);

        when(chain.filter(any()))
                .thenReturn(
                        ReactiveSecurityContextHolder
                                .getContext()
                                .flatMap(context -> {

                                    var authentication =
                                            context.getAuthentication();

                                    org.junit.jupiter.api.Assertions.assertEquals(
                                            "admin",
                                            authentication.getName()
                                    );

                                    org.junit.jupiter.api.Assertions.assertTrue(
                                            authentication.getAuthorities()
                                                    .stream()
                                                    .anyMatch(authority ->
                                                            authority.getAuthority()
                                                                    .equals("ROLE_ADMIN")
                                                    )
                                    );

                                    return Mono.empty();
                                })
                );

        StepVerifier.create(
                        jwtAuthenticationFilter.filter(exchange, chain)
                )
                .verifyComplete();

        verify(chain).filter(exchange);
    }

    private String createToken(
            String username,
            String role
    ) {
        return Jwts.builder()
                .setSubject(username)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(
                        new Date(
                                System.currentTimeMillis() + 60_000
                        )
                )
                .signWith(signingKey)
                .compact();
    }
}
