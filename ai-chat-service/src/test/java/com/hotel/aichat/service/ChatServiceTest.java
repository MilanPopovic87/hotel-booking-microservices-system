package com.hotel.aichat.service;

import com.hotel.aichat.agent.HotelBookingAgent;
import com.hotel.aichat.kafka.AuditEventProducer;
import com.hotel.aichat.dto.AuditEventRequest;
import com.hotel.aichat.dto.AuditEventType;
import com.hotel.aichat.exception.AiAuthenticationException;
import com.hotel.aichat.exception.AiRateLimitException;
import com.hotel.aichat.provider.AiExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChatServiceTest {

    private HotelBookingAgent hotelBookingAgent;
    private AuditEventProducer auditEventProducer;
    private AiExceptionHandler aiExceptionHandler;
    private ChatService chatService;

    @BeforeEach
    void setUp() {
        hotelBookingAgent = mock(HotelBookingAgent.class);
        auditEventProducer = mock(AuditEventProducer.class);
        aiExceptionHandler = mock(AiExceptionHandler.class);

        chatService = new ChatService(
                hotelBookingAgent,
                auditEventProducer,
                aiExceptionHandler
        );

        when(aiExceptionHandler.translate(any(Throwable.class)))
                .thenAnswer(invocation -> {
                    Throwable exception = invocation.getArgument(0);

                    return exception instanceof RuntimeException runtimeException
                            ? runtimeException
                            : new RuntimeException(exception);
                });
    }

    @Test
    void shouldReturnAiResponse() {
        String message = "How many bookings are there?";
        String expectedResponse = "There are 5 bookings.";

        when(hotelBookingAgent.run(
                message, "milan", "ADMIN", "chat-milan"
        )).thenReturn(expectedResponse);

        String result = chatService.ask(message, "milan", "ADMIN");

        assertEquals(expectedResponse, result);

        verify(hotelBookingAgent).run(
                message, "milan", "ADMIN", "chat-milan"
        );
    }

    @Test
    void shouldPropagateAiRateLimitException() {
        String message = "Hello";

        RuntimeException originalException =
                new RuntimeException("AI provider error");

        AiRateLimitException translatedException =
                new AiRateLimitException(
                        "AI usage limit reached. Please try again shortly.",
                        originalException
                );

        when(hotelBookingAgent.run(
                message, "milan", "ADMIN", "chat-milan"
        )).thenThrow(originalException);

        when(aiExceptionHandler.translate(originalException))
                .thenReturn(translatedException);

        AiRateLimitException thrown = assertThrows(
                AiRateLimitException.class,
                () -> chatService.ask(message, "milan", "ADMIN")
        );

        assertSame(translatedException, thrown);
        verify(aiExceptionHandler).translate(originalException);

        ArgumentCaptor<AuditEventRequest> captor =
                ArgumentCaptor.forClass(AuditEventRequest.class);

        verify(auditEventProducer, times(2)).send(captor.capture());

        List<AuditEventRequest> events = captor.getAllValues();

        assertEquals(AuditEventType.AI_REQUEST,
                events.get(0).getEventType());
        assertEquals(AuditEventType.AI_RATE_LIMITED,
                events.get(1).getEventType());
    }

    @Test
    void shouldPropagateAiAuthenticationException() {
        String message = "Hello";

        RuntimeException originalException =
                new RuntimeException("AI provider authentication failed");

        AiAuthenticationException translatedException =
                new AiAuthenticationException(
                        "AI provider authentication failed",
                        originalException
                );

        when(hotelBookingAgent.run(
                message, "milan", "ADMIN", "chat-milan"
        )).thenThrow(originalException);

        when(aiExceptionHandler.translate(originalException))
                .thenReturn(translatedException);

        AiAuthenticationException thrown = assertThrows(
                AiAuthenticationException.class,
                () -> chatService.ask(message, "milan", "ADMIN")
        );

        assertSame(translatedException, thrown);

        ArgumentCaptor<AuditEventRequest> captor =
                ArgumentCaptor.forClass(AuditEventRequest.class);

        verify(auditEventProducer, times(2)).send(captor.capture());

        List<AuditEventRequest> events = captor.getAllValues();

        assertEquals(AuditEventType.AI_REQUEST,
                events.get(0).getEventType());
        assertEquals(AuditEventType.AI_ERROR,
                events.get(1).getEventType());
    }

    @Test
    void shouldPropagateNonRateLimitException() {
        String message = "Hello";
        RuntimeException exception =
                new RuntimeException("AI service unavailable");

        when(hotelBookingAgent.run(
                message, "milan", "ADMIN", "chat-milan"
        )).thenThrow(exception);

        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> chatService.ask(message, "milan", "ADMIN")
        );

        assertSame(exception, thrown);
        verify(aiExceptionHandler).translate(exception);
    }

    @Test
    void shouldPublishAuditEventsForSuccessfulAiRequest() {
        String message = "How many bookings are there?";
        String expectedResponse = "There are 5 bookings.";

        when(hotelBookingAgent.run(
                message, "milan", "ADMIN", "chat-milan"
        )).thenReturn(expectedResponse);

        String result = chatService.ask(message, "milan", "ADMIN");

        assertEquals(expectedResponse, result);

        ArgumentCaptor<AuditEventRequest> captor =
                ArgumentCaptor.forClass(AuditEventRequest.class);

        verify(auditEventProducer, times(2)).send(captor.capture());

        List<AuditEventRequest> events = captor.getAllValues();

        assertEquals(AuditEventType.AI_REQUEST,
                events.get(0).getEventType());
        assertEquals(AuditEventType.AI_RESPONSE,
                events.get(1).getEventType());

        assertEquals("milan", events.get(0).getActor());
        assertEquals("milan", events.get(1).getActor());

        assertEquals("ai-chat-service", events.get(0).getServiceName());
        assertEquals("ai-chat-service", events.get(1).getServiceName());
    }
}