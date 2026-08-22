package com.hotel.aichat.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("gemini")
public class GeminiConfigurationValidator {

    public GeminiConfigurationValidator(
            @Value("${spring.ai.google.genai.api-key:}") String apiKey) {

        if (apiKey.isBlank()) {
            throw new IllegalStateException(
                    "GEMINI_API_KEY must be configured when the Gemini profile is enabled."
            );
        }
    }
}
