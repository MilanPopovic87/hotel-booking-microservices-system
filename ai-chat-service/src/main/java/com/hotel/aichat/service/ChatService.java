package com.hotel.aichat.service;

import com.hotel.aichat.agent.HotelBookingAgent;
import com.hotel.aichat.dto.AuditEventRequest;
import com.hotel.aichat.dto.AuditEventType;
import com.hotel.aichat.exception.AiAuthenticationException;
import com.hotel.aichat.exception.AiRateLimitException;
import com.hotel.aichat.kafka.AuditEventProducer;
import com.hotel.aichat.provider.AiExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class ChatService {

    private static final Logger log =
            LoggerFactory.getLogger(ChatService.class);

    private final HotelBookingAgent hotelBookingAgent;
    private final AuditEventProducer auditEventProducer;
    private final AiExceptionHandler aiExceptionHandler;

    public ChatService(
            HotelBookingAgent hotelBookingAgent,
            AuditEventProducer auditEventProducer,
            AiExceptionHandler aiExceptionHandler
    ) {

        this.hotelBookingAgent = hotelBookingAgent;
        this.auditEventProducer = auditEventProducer;
        this.aiExceptionHandler = aiExceptionHandler;
    }

    public String ask(String message, String username, String role) {

        String conversationId = "chat-" + username;

        // Record that an AI operation was requested.
        publishAuditEvent(
                AuditEventType.AI_REQUEST,
                username,
                role,
                Map.of("messageLength", message.length()),
                "AI request started");
        try {
            String answer = hotelBookingAgent.run(
                    message,
                    username,
                    role,
                    conversationId
            );

            // Record successful AI operation.
            publishAuditEvent(
                    AuditEventType.AI_RESPONSE,
                    username,
                    role,
                    Map.of(
                            "responseLength",
                            answer != null ? answer.length() : 0
                    ),
                    "AI response generated successfully"
            );

            return answer;

        } catch (RuntimeException e) {

            RuntimeException translated = aiExceptionHandler.translate(e);

            if (translated instanceof AiRateLimitException) {
                publishAuditEvent(
                        AuditEventType.AI_RATE_LIMITED,
                        username,
                        role,
                        Map.of("statusCode", 429),
                        "AI provider rate limit reached"
                );
            } else if (translated instanceof AiAuthenticationException) {
                publishAuditEvent(
                        AuditEventType.AI_ERROR,
                        username,
                        role,
                        Map.of("statusCode", 401),
                        "AI provider authentication failed"
                );
            } else {
                publishAuditEvent(
                        AuditEventType.AI_ERROR,
                        username,
                        role,
                        Map.of(
                                "errorType",
                                translated.getClass().getSimpleName()
                        ),
                        "AI request failed"
                );
            }

            throw translated;
        }
    }

    private void publishAuditEvent(
            AuditEventType eventType,
            String username,
            String role,
            Map<String, Object> payload,
            String message) {

        AuditEventRequest auditEvent = new AuditEventRequest(
                UUID.randomUUID(),
                eventType,
                "ai-chat-service",
                username,
                "AI_CHAT",
                null,
                payloadWithRole(payload, role),
                message
        );

        try {

            auditEventProducer.send(auditEvent);

        } catch (Exception e) {

            log.error(
                    "Failed to publish AI audit event {} for user {}",
                    eventType,
                    username,
                    e
            );
        }
    }

    private Map<String, Object> payloadWithRole(
            Map<String, Object> payload,
            String role) {

        Map<String, Object> result =
                new HashMap<>(payload);

        result.put("role", role);

        return result;
    }

}
