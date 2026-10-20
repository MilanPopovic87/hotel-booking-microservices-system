package com.hotel.aichat.agent;

import com.hotel.aichat.confirmation.ConfirmationService;
import com.hotel.aichat.confirmation.PendingConfirmation;
import com.hotel.aichat.dto.BookingUpdateResult;
import com.hotel.aichat.tools.AuditTools;
import com.hotel.aichat.tools.BookingTools;
import com.hotel.aichat.tools.UserTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class HotelBookingAgent {

    private final ChatClient chatClient;

    private final BookingTools bookingTools;
    private final UserTools userTools;
    private final AuditTools auditTools;
    private final ConfirmationService confirmationService;

    public HotelBookingAgent(
            ChatClient.Builder builder,
            BookingTools bookingTools,
            UserTools userTools,
            AuditTools auditTools,
            ConfirmationService confirmationService
    ) {
        this.chatClient = builder.build();
        this.bookingTools = bookingTools;
        this.userTools = userTools;
        this.auditTools = auditTools;
        this.confirmationService = confirmationService;
    }

    public String run(
            String message,
            String username,
            String role,
            String conversationId
    ) {

        PendingConfirmation pendingConfirmation =
                confirmationService.getPendingConfirmation(
                        conversationId
                );

        if (pendingConfirmation != null) {

            return handlePendingConfirmation(
                    message,
                    username,
                    role,
                    conversationId,
                    pendingConfirmation
            );
        }

        String systemPrompt = """
            You are an AI assistant for a hotel management system.

            The current authenticated user is %s.
            The user's role is %s.

            Your goal is to help the user understand
            information from the hotel management system.

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
            """.formatted(username, role);

        return chatClient.prompt()
                .system(systemPrompt)
                .tools(
                        bookingTools,
                        userTools,
                        auditTools
                )
                .toolContext(Map.of(
                        "username", username,
                        "role", role,
                        "conversationId", conversationId
                ))
                .advisors(advisor -> advisor
                        .param(
                                ChatMemory.CONVERSATION_ID,
                                conversationId
                        )
                )
                .user(message)
                .call()
                .content();
    }

    // Confirmation handling

    private boolean isYes(String message) {
        return message.trim().equalsIgnoreCase("yes");
    }

    private boolean isNo(String message) {
        return message.trim().equalsIgnoreCase("no");
    }

    private String handlePendingConfirmation(
            String message,
            String username,
            String role,
            String conversationId,
            PendingConfirmation pendingConfirmation) {

        if (isYes(message)) {

            ToolContext toolContext = new ToolContext(Map.of(
                    "username", username,
                    "role", role,
                    "conversationId", conversationId
            ));

            BookingUpdateResult result =
                    bookingTools.executeConfirmedUpdate(
                            pendingConfirmation,
                            toolContext
                    );

            return result.message();
        }

        if (isNo(message)) {

            confirmationService.clearConfirmation(
                    conversationId
            );

            return "The booking update has been cancelled.";
        }

        return "Please reply YES to confirm or NO to cancel.";
    }
}
