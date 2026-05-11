package com.hotel.booking.controller;

import com.hotel.booking.dto.BookingRequestDTO;
import com.hotel.booking.dto.BookingResponseDTO;
import com.hotel.booking.entity.Booking;
import com.hotel.booking.service.BookingService;
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
    public List<BookingResponseDTO> getAllBookings() {
        return bookingService.getAllBookings().stream()
                .map(BookingResponseDTO::new)
                .toList();
    }

    // Get booking by ID
    @GetMapping("/{id}")
    public BookingResponseDTO getBookingById(@PathVariable Long id) {
        return new BookingResponseDTO(
                bookingService.getBookingById(id)
        );
    }

    // Get my bookings
    @GetMapping("/my")
    public List<BookingResponseDTO> getMyBookings(Authentication authentication) {
        return bookingService.getMyBookings(authentication)
                .stream()
                .map(BookingResponseDTO::new)
                .toList();
    }

    // Get bookings by room
    @GetMapping("/by-room/{roomId}")
    public List<BookingResponseDTO> getBookingsByRoom(@PathVariable Long roomId) {
        return bookingService.getBookingsByRoomId(roomId).stream()
                .map(BookingResponseDTO::new)
                .toList();
    }

    // INTERNAL ONLY: Used by other services (e.g. User Service) to verify whether a user has bookings
    @GetMapping("/internal/users/{userId}/has-bookings")
    public boolean hasBookings(@PathVariable Long userId) {
        return bookingService.existsByUserId(userId);
    }

    // Create a booking
    @PostMapping
    public BookingResponseDTO create(@RequestBody BookingRequestDTO dto, Authentication authentication) {
        Booking booking = bookingService.createBooking(dto, authentication);
        return new BookingResponseDTO(booking);
    }

    // Update a booking
    // Reserved for future admin booking edits
    @PutMapping("/{id}")
    public BookingResponseDTO updateBooking(
            @PathVariable Long id,
            @RequestBody BookingRequestDTO dto
    ) {
        Booking booking = bookingService.updateBooking(id, dto);
        return new BookingResponseDTO(booking);
    }

    // Delete a booking
    @DeleteMapping("/{id}")
    public void deleteBooking(@PathVariable Long id) {
        bookingService.deleteBooking(id);
    }
}
