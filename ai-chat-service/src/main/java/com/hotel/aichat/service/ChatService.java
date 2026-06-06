package com.hotel.aichat.service;

import com.hotel.aichat.tools.AuditTools;
import com.hotel.aichat.tools.BookingTools;
import com.hotel.aichat.tools.UserTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    private final ChatClient chatClient;

    private final BookingTools bookingTools;
    private final UserTools userTools;
    private final AuditTools auditTools;
    private final ChatMemory chatMemory;

    public ChatService(
            ChatClient.Builder builder,
            BookingTools bookingTools,
            UserTools userTools,
            AuditTools auditTools,
            ChatMemory chatMemory
    ) {

        this.chatClient = builder.build();

        this.bookingTools = bookingTools;
        this.userTools = userTools;
        this.auditTools = auditTools;
        this.chatMemory = chatMemory;
    }

    public String ask(String message) {

        String systemPrompt = """
            You are an AI assistant for a hotel management system.

            You can access:
            - bookings
            - users
            - audit events

            Use tools whenever data is required.

            Never invent bookings, users, or audit events.
            If information is unavailable, say so.
            """;

        return chatClient.prompt()
                .system(systemPrompt)
                .tools(
                        bookingTools,
                        userTools,
                        auditTools
                )
                .advisors(advisor -> advisor
                        .param(ChatMemory.CONVERSATION_ID, "admin-chat")
                )
                .user(message)
                .call()
                .content();
    }
}
