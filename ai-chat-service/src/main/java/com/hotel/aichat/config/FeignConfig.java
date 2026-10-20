package com.hotel.aichat.config;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor internalServiceTokenInterceptor(
            @Value("${ai.service-token}") String serviceToken) {

        return requestTemplate ->
                requestTemplate.header(
                        "X-Internal-Service-Token",
                        serviceToken
                );
    }
}
