package com.hotel.aichat.service;

import com.hotel.aichat.dto.AuditEventRequest;
import com.hotel.aichat.dto.AuditEventType;
import com.hotel.aichat.exception.AiAuthenticationException;
import com.hotel.aichat.exception.AiRateLimitException;
import com.hotel.aichat.kafka.AuditEventProducer;
import com.hotel.aichat.provider.AiExceptionHandler;
import com.hotel.aichat.tools.AuditTools;
import com.hotel.aichat.tools.BookingTools;
import com.hotel.aichat.tools.UserTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class ChatService {

    private static final Logger log =
            LoggerFactory.getLogger(ChatService.class);

    private final ChatClient chatClient;
    private final BookingTools bookingTools;
    private final UserTools userTools;
    private final AuditTools auditTools;
    private final ChatMemory chatMemory;
    private final AuditEventProducer auditEventProducer;
    private final AiExceptionHandler aiExceptionHandler;

    public ChatService(
            ChatClient.Builder builder,
            BookingTools bookingTools,
            UserTools userTools,
            AuditTools auditTools,
            ChatMemory chatMemory,
            AuditEventProducer auditEventProducer,
            AiExceptionHandler aiExceptionHandler
    ) {

        this.chatClient = builder.build();
        this.bookingTools = bookingTools;
        this.userTools = userTools;
        this.auditTools = auditTools;
        this.chatMemory = chatMemory;
        this.auditEventProducer = auditEventProducer;
        this.aiExceptionHandler = aiExceptionHandler;
    }

    public String ask(String message, String username, String role) {

        String conversationId = "chat-" + username;

        String systemPrompt = """
            You are an AI assistant for a hotel management system.
            
            The current authenticated user is %s.
            The user's role is %s.

            You can access hotel management data through tools:
            - bookings
            - users
            - audit events

            Rules:
            - Use tools whenever information from the hotel management system is required.
            - Never invent or guess system data.
            - If information is unavailable, clearly say so.
            - Use clear and concise formatting.
            - For lists of records, use bullet points or tables when appropriate.
            - If the user's request is ambiguous, ask for clarification.
            - Do not reveal internal tool names or implementation details.
            """.formatted(username, role);;

        // Record that an AI operation was requested.
        publishAuditEvent(
                AuditEventType.AI_REQUEST,
                username,
                role,
                Map.of("messageLength", message.length()),
                "AI request started");
        try {
            String answer = chatClient.prompt()
                    .system(systemPrompt)
                    .tools(
                            bookingTools,
                            userTools,
                            auditTools
                    )
                    .advisors(advisor -> advisor
                            .param(ChatMemory.CONVERSATION_ID, conversationId)
                    )
                    .user(message)
                    .call()
                    .content();

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

    private boolean hasStatusCode(Throwable exception, int statusCode) {

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
}
