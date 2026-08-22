package com.hotel.aichat.provider;

public interface AiExceptionHandler {

    RuntimeException translate(Throwable exception);
}
