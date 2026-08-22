package com.hotel.aichat.provider;

import com.google.genai.errors.ClientException;
import com.hotel.aichat.exception.AiAuthenticationException;
import com.hotel.aichat.exception.AiRateLimitException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GeminiExceptionHandlerTest {

    private GeminiExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GeminiExceptionHandler();
    }

    @Test
    void shouldTranslate401ToAiAuthenticationException() {

        ClientException exception =
                new ClientException(
                        401,
                        "Invalid authentication credentials",
                        null
                );

        RuntimeException result =
                exceptionHandler.translate(exception);

        assertInstanceOf(
                AiAuthenticationException.class,
                result
        );

        assertEquals(
                "AI provider authentication failed.",
                result.getMessage()
        );

        assertSame(
                exception,
                result.getCause()
        );
    }

    @Test
    void shouldTranslate429ToAiRateLimitException() {

        ClientException exception =
                new ClientException(
                        429,
                        "Quota exceeded",
                        null
                );

        RuntimeException result =
                exceptionHandler.translate(exception);

        assertInstanceOf(
                AiRateLimitException.class,
                result
        );

        assertEquals(
                "AI usage limit reached. Please try again shortly.",
                result.getMessage()
        );

        assertSame(
                exception,
                result.getCause()
        );
    }

    @Test
    void shouldReturnOriginalRuntimeExceptionForUnhandledStatus() {

        ClientException exception =
                new ClientException(
                        500,
                        "Internal server error",
                        null
                );

        RuntimeException result =
                exceptionHandler.translate(exception);

        assertSame(exception, result);
    }
}
