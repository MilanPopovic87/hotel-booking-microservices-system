package com.hotel.aichat.agent;

import com.hotel.aichat.confirmation.ConfirmationOperation;
import com.hotel.aichat.confirmation.ConfirmationService;
import com.hotel.aichat.confirmation.PendingConfirmation;
import com.hotel.aichat.dto.BookingUpdateResult;
import com.hotel.aichat.tools.AuditTools;
import com.hotel.aichat.tools.BookingTools;
import com.hotel.aichat.tools.UserTools;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ToolContext;

import java.time.LocalDate;
import java.util.Map;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HotelBookingAgentTest {

    private static final String USERNAME = "milan";
    private static final String ROLE = "ADMIN";
    private static final String CONVERSATION_ID = "chat-milan";

    private ChatClient.Builder chatClientBuilder;
    private ChatClient chatClient;
    private ChatClient.ChatClientRequestSpec requestSpec;
    private ChatClient.CallResponseSpec responseSpec;

    private BookingTools bookingTools;
    private UserTools userTools;
    private AuditTools auditTools;
    private ConfirmationService confirmationService;

    private HotelBookingAgent hotelBookingAgent;

    @BeforeEach
    void setUp() {
        chatClientBuilder = mock(ChatClient.Builder.class);
        chatClient = mock(ChatClient.class);
        requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        responseSpec = mock(ChatClient.CallResponseSpec.class);

        bookingTools = mock(BookingTools.class);
        userTools = mock(UserTools.class);
        auditTools = mock(AuditTools.class);
        confirmationService = mock(ConfirmationService.class);

        when(chatClientBuilder.build()).thenReturn(chatClient);

        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.tools(
                bookingTools,
                userTools,
                auditTools
        )).thenReturn(requestSpec);
        when(requestSpec.toolContext(any(Map.class)))
                .thenReturn(requestSpec);
        when(requestSpec.advisors(any(Consumer.class)))
                .thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(responseSpec);

        hotelBookingAgent = new HotelBookingAgent(
                chatClientBuilder,
                bookingTools,
                userTools,
                auditTools,
                confirmationService
        );
    }

    @Test
    void shouldBuildChatClientDuringConstruction() {
        verify(chatClientBuilder).build();
    }

    @Test
    void shouldRunAiRequestWithAuthenticatedUserContext() {
        String message = "How many bookings are there?";
        String expectedResponse = "There are 5 bookings.";

        when(responseSpec.content()).thenReturn(expectedResponse);

        String result = hotelBookingAgent.run(
                message,
                USERNAME,
                ROLE,
                CONVERSATION_ID
        );

        assertEquals(expectedResponse, result);

        verify(confirmationService)
                .getPendingConfirmation(CONVERSATION_ID);

        ArgumentCaptor<String> systemPromptCaptor =
                ArgumentCaptor.forClass(String.class);

        verify(requestSpec).system(systemPromptCaptor.capture());

        String systemPrompt = systemPromptCaptor.getValue();

        assertTrue(systemPrompt.contains(USERNAME));
        assertTrue(systemPrompt.contains(ROLE));

        verify(requestSpec).tools(
                bookingTools,
                userTools,
                auditTools
        );

        verify(requestSpec).toolContext(Map.of(
                "username", USERNAME,
                "role", ROLE,
                "conversationId", CONVERSATION_ID
        ));

        verify(requestSpec).advisors(any(Consumer.class));
        verify(requestSpec).user(message);
        verify(requestSpec).call();
        verify(responseSpec).content();
    }

    @Test
    void shouldExecuteConfirmedUpdateWhenUserRepliesYes() {
        PendingConfirmation pending = createPendingConfirmation();

        BookingUpdateResult updateResult = mock(BookingUpdateResult.class);

        when(confirmationService.getPendingConfirmation(CONVERSATION_ID))
                .thenReturn(pending);

        when(updateResult.message())
                .thenReturn("Booking updated successfully.");

        when(bookingTools.executeConfirmedUpdate(
                any(PendingConfirmation.class),
                any(ToolContext.class)
        )).thenReturn(updateResult);

        // Return the mocked result from the tool invocation.
        String result = hotelBookingAgent.run(
                "YES",
                USERNAME,
                ROLE,
                CONVERSATION_ID
        );

        assertEquals("Booking updated successfully.", result);

        ArgumentCaptor<ToolContext> contextCaptor =
                ArgumentCaptor.forClass(ToolContext.class);

        verify(bookingTools).executeConfirmedUpdate(
                org.mockito.ArgumentMatchers.eq(pending),
                contextCaptor.capture()
        );

        verify(chatClient, never()).prompt();
        verify(confirmationService, never())
                .clearConfirmation(CONVERSATION_ID);
    }

    @Test
    void shouldCancelPendingUpdateWhenUserRepliesNo() {
        PendingConfirmation pending = createPendingConfirmation();

        when(confirmationService.getPendingConfirmation(CONVERSATION_ID))
                .thenReturn(pending);

        String result = hotelBookingAgent.run(
                "NO",
                USERNAME,
                ROLE,
                CONVERSATION_ID
        );

        assertEquals(
                "The booking update has been cancelled.",
                result
        );

        verify(confirmationService)
                .clearConfirmation(CONVERSATION_ID);

        verify(bookingTools, never()).executeConfirmedUpdate(
                any(PendingConfirmation.class),
                any(ToolContext.class)
        );

        verify(chatClient, never()).prompt();
    }

    @Test
    void shouldAskForYesOrNoWhenConfirmationReplyIsUnclear() {
        PendingConfirmation pending = createPendingConfirmation();

        when(confirmationService.getPendingConfirmation(CONVERSATION_ID))
                .thenReturn(pending);

        String result = hotelBookingAgent.run(
                "Maybe later",
                USERNAME,
                ROLE,
                CONVERSATION_ID
        );

        assertEquals(
                "Please reply YES to confirm or NO to cancel.",
                result
        );

        verify(bookingTools, never()).executeConfirmedUpdate(
                any(PendingConfirmation.class),
                any(ToolContext.class)
        );

        verify(confirmationService, never())
                .clearConfirmation(CONVERSATION_ID);

        verify(chatClient, never()).prompt();
    }

    @Test
    void shouldUseNormalAiFlowWhenNoConfirmationIsPending() {
        String expectedResponse = "Hello! How can I help?";

        when(responseSpec.content()).thenReturn(expectedResponse);

        String result = hotelBookingAgent.run(
                "yes",
                USERNAME,
                ROLE,
                CONVERSATION_ID
        );

        assertEquals(expectedResponse, result);

        verify(confirmationService)
                .getPendingConfirmation(CONVERSATION_ID);

        verify(chatClient).prompt();

        verify(bookingTools, never()).executeConfirmedUpdate(
                any(PendingConfirmation.class),
                any(ToolContext.class)
        );
    }

    private PendingConfirmation createPendingConfirmation() {
        return new PendingConfirmation(
                ConfirmationOperation.UPDATE_BOOKING,
                123L,
                456L,
                LocalDate.of(2026, 11, 10),
                LocalDate.of(2026, 11, 15)
        );
    }
}
