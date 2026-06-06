package com.hotel.booking.controller;

import com.hotel.booking.dto.BookingRequest;
import com.hotel.booking.dto.BookingResponse;
import com.hotel.booking.service.BookingService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    // Get all bookings
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<BookingResponse> getAllBookings() {
        return bookingService.getAllBookings();
    }

    @GetMapping("/internal/all")
    public List<BookingResponse> getAllBookingsInternal() {
        return bookingService.getAllBookings();
    }

    // Get booking by ID
    @GetMapping("/{id}")
    public BookingResponse getBookingById(@PathVariable Long id) {
        return bookingService.getBookingById(id);
    }

    // Get my bookings
    @GetMapping("/my")
    public List<BookingResponse> getMyBookings(Authentication authentication) {
        return bookingService.getMyBookings(authentication);
    }

    // Get bookings by room
    @GetMapping("/by-room/{roomId}")
    @PreAuthorize("hasRole('ADMIN')")
    public List<BookingResponse> getBookingsByRoom(@PathVariable Long roomId) {
        return bookingService.getBookingsByRoomId(roomId);
    }

    @GetMapping("/internal/by-room/{roomId}")
    public List<BookingResponse> getBookingsByRoomInternal(@PathVariable Long roomId) {
        return bookingService.getBookingsByRoomId(roomId);
    }

    // INTERNAL ONLY: Used by other services (e.g. User Service) to verify whether a user has bookings
    @GetMapping("/internal/users/{userId}/has-bookings")
    public boolean hasBookings(@PathVariable Long userId) {
        return bookingService.existsByUserId(userId);
    }

    // Create a booking
    @PostMapping
    public BookingResponse create(@RequestBody BookingRequest dto, Authentication authentication) {
        return bookingService.createBooking(dto, authentication);
    }

    // Update a booking
    // Reserved for future admin booking edits
    @PutMapping("/{id}")
    public BookingResponse updateBooking(@PathVariable Long id, @RequestBody BookingRequest dto,
                                         Authentication authentication) {
        return bookingService.updateBooking(id, dto, authentication);
    }

    // Delete a booking
    @DeleteMapping("/{id}")
    public void deleteBooking(@PathVariable Long id, Authentication authentication) {
        bookingService.deleteBooking(id,  authentication);
    }
}
