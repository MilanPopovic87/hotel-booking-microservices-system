package com.hotel.aichat.dto;

public record BookingUpdateResult(
        String status,
        String message,
        BookingResponse booking
) {
}
