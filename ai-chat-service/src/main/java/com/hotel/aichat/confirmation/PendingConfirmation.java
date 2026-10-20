package com.hotel.aichat.confirmation;

import java.time.LocalDate;

public record PendingConfirmation(
        ConfirmationOperation operation,
        Long bookingId,
        Long roomId,
        LocalDate checkInDate,
        LocalDate checkOutDate
) {
}