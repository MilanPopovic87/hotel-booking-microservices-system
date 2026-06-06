package com.hotel.aichat.dto;

import java.time.LocalDate;

public record BookingResponse(
        Long id,
        Long userId,
        Long roomId,
        String username,
        String roomName,
        LocalDate checkInDate,
        LocalDate checkOutDate
) {
}
