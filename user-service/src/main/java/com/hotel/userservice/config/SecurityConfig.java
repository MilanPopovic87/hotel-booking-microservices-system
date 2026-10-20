package com.hotel.userservice.config;

import com.hotel.userservice.security.InternalServiceAuthenticationFilter;
import com.hotel.userservice.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final InternalServiceAuthenticationFilter internalServiceAuthenticationFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter,
                          InternalServiceAuthenticationFilter internalServiceAuthenticationFilter) {

        this.jwtAuthFilter = jwtAuthFilter;
        this.internalServiceAuthenticationFilter = internalServiceAuthenticationFilter;
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

                        // Public endpoints (login/register)
                        .requestMatchers("/api/auth/**").permitAll()

                        // Internal microservice communication
                        .requestMatchers("/api/users/internal/**").permitAll()

                        // Any authenticated user can access their own profile
                        .requestMatchers("/api/users/me").authenticated()


                        // Users: only ADMIN can manage users
                        .requestMatchers("/api/users/**").hasRole("ADMIN")


                        // Everything else requires authentication
                        .anyRequest().authenticated()
                )

                .addFilterBefore(internalServiceAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // Validate JWT after internal service authentication
                // and before UsernamePasswordAuthenticationFilter.
                .addFilterAfter(jwtAuthFilter, InternalServiceAuthenticationFilter.class)

                .build();
    }

    // Password encoder for hashing passwords
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
