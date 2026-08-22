package com.hotel.aichat.provider;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("ollama")
public class OllamaExceptionHandler
        implements AiExceptionHandler {

    @Override
    public RuntimeException translate(Throwable exception) {
        return exception instanceof RuntimeException runtimeException
                ? runtimeException
                : new RuntimeException(exception);
    }
}
