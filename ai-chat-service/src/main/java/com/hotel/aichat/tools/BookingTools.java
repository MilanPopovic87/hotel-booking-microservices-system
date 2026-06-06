package com.hotel.aichat.tools;

import com.hotel.aichat.client.BookingClient;
import com.hotel.aichat.dto.BookingResponse;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BookingTools {

    private final BookingClient bookingClient;

    public BookingTools(BookingClient bookingClient) {
        this.bookingClient = bookingClient;
    }

    @Tool(description = "Returns all bookings in the hotel system")
    public List<BookingResponse> getAllBookings() {
        return bookingClient.getAllBookings();
    }

    @Tool(description = "Returns bookings for a specific room id")
    public List<BookingResponse> getBookingsByRoom(Long roomId) {
        return bookingClient.getBookingsByRoom(roomId);
    }
}
