package com.hotel.booking.dto;

import com.hotel.booking.entity.Booking;

import java.time.LocalDate;

public class BookingResponseDTO {

    private Long id;

    private Long userId;

    private Long roomId;

    private LocalDate checkInDate;
    private LocalDate checkOutDate;

    public BookingResponseDTO() {
    }

    public BookingResponseDTO(Booking booking) {
        this.id = booking.getId();
        this.userId = booking.getUserId();
        this.roomId = booking.getRoomId();
        this.checkInDate = booking.getCheckInDate();
        this.checkOutDate = booking.getCheckOutDate();
    }

    // ===== Getters =====

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getRoomId() {
        return roomId;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    // ===== Setters =====

    public void setId(Long id) {
        this.id = id;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public void setCheckInDate(LocalDate checkInDate) {
        this.checkInDate = checkInDate;
    }

    public void setCheckOutDate(LocalDate checkOutDate) {
        this.checkOutDate = checkOutDate;
    }
}
