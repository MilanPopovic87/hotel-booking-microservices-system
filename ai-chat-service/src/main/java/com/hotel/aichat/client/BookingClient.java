package com.hotel.aichat.client;

import com.hotel.aichat.dto.BookingResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "booking-service", url = "${services.booking.url}")
public interface BookingClient {

    @GetMapping("/api/bookings/internal/all")
    List<BookingResponse> getAllBookings();


    @GetMapping("/api/bookings/internal/by-room/{roomId}")
    List<BookingResponse> getBookingsByRoom(@PathVariable("roomId") Long roomId);
}

