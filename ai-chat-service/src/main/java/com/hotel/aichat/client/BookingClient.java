package com.hotel.aichat.client;

import com.hotel.aichat.dto.BookingRequest;
import com.hotel.aichat.dto.BookingResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(
        name = "booking-service",
        url = "${services.booking.url}"
)
public interface BookingClient {

    @GetMapping("/api/bookings/internal/ai/count")
    long getBookingCount();

    @GetMapping("/api/bookings/internal/ai/{id}")
    BookingResponse getBookingById(
            @PathVariable("id") Long id
    );

    @GetMapping("/api/bookings/internal/ai/by-room/{roomId}")
    List<BookingResponse> getBookingsByRoom(
            @PathVariable("roomId") Long roomId
    );

    @PutMapping("/api/bookings/internal/ai/{id}")
    BookingResponse updateBooking(
            @PathVariable("id") Long id,
            @RequestBody BookingRequest request,
            @RequestHeader("X-Authenticated-User") String username
    );
}

