package com.hotel.aichat.provider;

import com.hotel.aichat.exception.AiAuthenticationException;
import com.hotel.aichat.exception.AiRateLimitException;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("gemini")
public class GeminiExceptionHandler
        implements AiExceptionHandler {

    @Override
    public RuntimeException translate(Throwable exception) {

        if (hasStatusCode(exception, 401)) {
            return new AiAuthenticationException(
                    "AI provider authentication failed.",
                    exception
            );
        }

        if (hasStatusCode(exception, 429)) {
            return new AiRateLimitException(
                    "AI usage limit reached. Please try again shortly.",
                    exception
            );
        }

        return asRuntimeException(exception);
    }

    private boolean hasStatusCode(
            Throwable exception,
            int statusCode) {

        Throwable current = exception;

        while (current != null) {

            if (current instanceof com.google.genai.errors.ClientException clientException
                    && clientException.code() == statusCode) {
                return true;
            }

            current = current.getCause();
        }

        return false;
    }

    private RuntimeException asRuntimeException(Throwable exception) {
        return exception instanceof RuntimeException runtimeException
                ? runtimeException
                : new RuntimeException(exception);
    }
}
