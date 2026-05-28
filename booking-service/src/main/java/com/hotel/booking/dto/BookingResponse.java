package com.hotel.booking.dto;

import com.hotel.booking.entity.Booking;

import java.time.LocalDate;

public class BookingResponse {

    // System identifiers
    private final Long id;
    private final Long userId;
    private final Long roomId;

    // UI-friendly fields (enriched)
    private final String username;
    private final String roomName;

    // Booking data
    private final LocalDate checkInDate;
    private final LocalDate checkOutDate;

    public BookingResponse(Long id,
                           Long userId,
                           Long roomId,
                           String username,
                           String roomName,
                           LocalDate checkInDate,
                           LocalDate checkOutDate) {
        this.id = id;
        this.userId = userId;
        this.roomId = roomId;
        this.username = username;
        this.roomName = roomName;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
    }

    // Factory method (clean mapping style)
    public static BookingResponse of(Booking booking,
                                     String username,
                                     String roomName) {
        return new BookingResponse(
                booking.getId(),
                booking.getUserId(),
                booking.getRoomId(),
                username,
                roomName,
                booking.getCheckInDate(),
                booking.getCheckOutDate()
        );
    }

    // Getters only (immutable DTO)

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getRoomId() {
        return roomId;
    }

    public String getUsername() {
        return username;
    }

    public String getRoomName() {
        return roomName;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }
}