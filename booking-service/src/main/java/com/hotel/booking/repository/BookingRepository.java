package com.hotel.booking.repository;

import com.hotel.booking.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    // ================= FIND =================

    // All bookings for a specific user
    List<Booking> findByUserId(Long userId);

    // Method for existence checks
    boolean existsByUserId(Long userId);


    // All bookings for a specific room
    List<Booking> findByRoomId(Long roomId);

    // Method for existence checks
    boolean existsByRoomId(Long roomId);

    // ================= CHECK OVERLAPS =================

    // Checking for any existing booking overlaps (for CREATE)
    @Query("""
      SELECT COUNT(b) > 0
      FROM Booking b
      WHERE b.roomId = :roomId
        AND b.checkOutDate > :checkInDate
        AND b.checkInDate < :checkOutDate
    """)
    boolean existsByRoomAndDateOverlap(
            @Param("roomId") Long roomId,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate
    );

    // Checking for overlapping bookings excluding a specific booking (for UPDATE)
    @Query("""
      SELECT COUNT(b) > 0
      FROM Booking b
      WHERE b.roomId = :roomId
        AND b.id <> :bookingId
        AND b.checkOutDate > :checkInDate
        AND b.checkInDate < :checkOutDate
    """)
    boolean existsByRoomAndDateOverlapExcludingBooking(
            @Param("roomId") Long roomId,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate,
            @Param("bookingId") Long bookingId
    );
}
