package com.hotel.aichat.tools;

import com.hotel.aichat.client.BookingClient;
import com.hotel.aichat.confirmation.ConfirmationOperation;
import com.hotel.aichat.confirmation.ConfirmationService;
import com.hotel.aichat.confirmation.PendingConfirmation;
import com.hotel.aichat.dto.BookingRequest;
import com.hotel.aichat.dto.BookingResponse;
import com.hotel.aichat.dto.BookingUpdateResult;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BookingTools {

    private final BookingClient bookingClient;
    private final ConfirmationService confirmationService;

    public BookingTools(
            BookingClient bookingClient,
            ConfirmationService confirmationService) {

        this.bookingClient = bookingClient;
        this.confirmationService = confirmationService;
    }

    @Tool(description = """
        Returns the total number of bookings currently stored
        in the hotel booking system.
        Use this when the user asks how many bookings exist.
        """)
    public long getBookingCount() {
        return bookingClient.getBookingCount();
    }

    @Tool(description = """
        Returns a booking by its unique booking id.
        Use this when the user asks about a specific booking.
        """)
    public BookingResponse getBookingById(Long bookingId) {
        return bookingClient.getBookingById(bookingId);
    }

    @Tool(description = """
        Returns bookings associated with a specific room.
        Use this when the user asks about bookings for a particular room.
        """)
    public List<BookingResponse> getBookingsByRoom(Long roomId) {
        return bookingClient.getBookingsByRoom(roomId);
    }

    @Tool(description = """
        Prepares an existing booking for update with a new room
        and/or new check-in and check-out dates.

        This tool does not execute the booking update.
        The user must explicitly confirm the exact proposed change
        before the update can be executed.
        """)
    public BookingUpdateResult updateBooking(
            Long bookingId,
            BookingRequest bookingRequest,
            ToolContext toolContext) {

        String conversationId =
                (String) toolContext.getContext().get("conversationId");

        PendingConfirmation requestedConfirmation =
                new PendingConfirmation(
                        ConfirmationOperation.UPDATE_BOOKING,
                        bookingId,
                        bookingRequest.getRoomId(),
                        bookingRequest.getCheckInDate(),
                        bookingRequest.getCheckOutDate()
                );

        confirmationService.createPendingConfirmation(
                conversationId,
                requestedConfirmation
        );

        return new BookingUpdateResult(
                "CONFIRMATION_REQUIRED",
                """
                The requested booking change has not been executed.

                The user must explicitly confirm this exact change
                by replying YES. The user can reply NO to cancel it.
                """,
                null
        );
    }

    public BookingUpdateResult executeConfirmedUpdate(
            PendingConfirmation confirmation,
            ToolContext toolContext) {

        String username =
                (String) toolContext.getContext().get("username");

        String conversationId =
                (String) toolContext.getContext().get("conversationId");

        boolean confirmed =
                confirmationService.consumeIfMatches(
                        conversationId,
                        confirmation
                );

        if (!confirmed) {
            return new BookingUpdateResult(
                    "CONFIRMATION_REQUIRED",
                    "The booking confirmation is no longer valid.",
                    null
            );
        }

        BookingRequest bookingRequest =
                new BookingRequest();

        bookingRequest.setRoomId(
                confirmation.roomId()
        );

        bookingRequest.setCheckInDate(
                confirmation.checkInDate()
        );

        bookingRequest.setCheckOutDate(
                confirmation.checkOutDate()
        );

        return performUpdate(
                confirmation.bookingId(),
                bookingRequest,
                username
        );
    }

    private BookingUpdateResult performUpdate(
            Long bookingId,
            BookingRequest bookingRequest,
            String username) {

        BookingResponse updatedBooking =
                bookingClient.updateBooking(
                        bookingId,
                        bookingRequest,
                        username
                );

        return new BookingUpdateResult(
                "UPDATED",
                "The booking was successfully updated.",
                updatedBooking
        );
    }
}
