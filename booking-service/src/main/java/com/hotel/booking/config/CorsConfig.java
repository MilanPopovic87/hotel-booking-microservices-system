package com.hotel.booking.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@Profile("dev")
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Allow Angular frontend to access backend
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:4200"   // dev
                )
                .allowedMethods("*");
    }
}
