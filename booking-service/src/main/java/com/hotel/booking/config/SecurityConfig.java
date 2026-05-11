package com.hotel.booking.config;

import com.hotel.booking.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        return http
                // We use JWT (Authorization header), so CSRF is not needed
                .csrf(csrf -> csrf.disable())

                // Allow Angular frontend to call backend
                .cors(Customizer.withDefaults())

                // Stateless API: no sessions, every request must carry JWT
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth

                        // Rooms: everyone can view rooms
                        .requestMatchers(HttpMethod.GET, "/api/rooms/**").permitAll()

                        // internal microservice communication
                        .requestMatchers("/api/bookings/internal/**").permitAll()

                        // Rooms: only ADMIN can modify rooms
                        .requestMatchers(HttpMethod.POST, "/api/rooms/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/rooms/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/rooms/**").hasRole("ADMIN")

                        // Bookings: must be logged in
                        .requestMatchers("/api/bookings/**").authenticated()

                        // Everything else requires authentication
                        .anyRequest().authenticated()
                )

                // Validate JWT before Spring Security processes authentication
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

                .build();
    }

}
