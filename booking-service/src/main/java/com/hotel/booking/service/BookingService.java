package com.hotel.booking.service;

import com.hotel.booking.client.AuditClient;
import com.hotel.booking.dto.AuditEventRequest;
import com.hotel.booking.dto.AuditEventType;
import com.hotel.booking.dto.BookingRequest;
import com.hotel.booking.dto.BookingResponse;
import com.hotel.booking.entity.Booking;
import com.hotel.booking.entity.Room;
import com.hotel.booking.mapper.BookingMapper;
import com.hotel.booking.repository.BookingRepository;
import com.hotel.booking.security.CustomUserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.Authentication;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;
    private final RoomService roomService;
    private final AuditClient auditClient;
    private static final Logger log =
            LoggerFactory.getLogger(BookingService.class);

    public BookingService(
            BookingRepository bookingRepository,
            BookingMapper bookingMapper,
            RoomService roomService,
            AuditClient auditClient
    ) {
        this.bookingRepository = bookingRepository;
        this.bookingMapper = bookingMapper;
        this.roomService = roomService;
        this.auditClient = auditClient;
    }

    // ================= READ =================

    @PreAuthorize("hasRole('ADMIN')")
    public List<BookingResponse> getAllBookings() {

        return bookingRepository.findAll()
                .stream()
                .map(bookingMapper::toResponse)
                .toList();
    }

    @PreAuthorize("hasRole('ADMIN') or @bookingService.isOwner(#id, authentication.principal.id)")
    public BookingResponse getBookingById(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Booking not found"
                ));
        return bookingMapper.toResponse(booking);
    }

    public List<BookingResponse> getMyBookings(Authentication authentication) {

        CustomUserPrincipal user =
                (CustomUserPrincipal) authentication.getPrincipal();

        return bookingRepository.findByUserId(user.getId())
                .stream()
                .map(bookingMapper::toResponse)
                .toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<BookingResponse> getBookingsByRoomId(Long roomId) {

        return bookingRepository.findByRoomId(roomId)
                .stream()
                .map(bookingMapper::toResponse)
                .toList();
    }

    public boolean existsByUserId(Long userId) {
        return bookingRepository.existsByUserId(userId);
    }

    // ================= CREATE =================

    /**
     * Creates a new booking.
     * Uses @Transactional to prevent race conditions (double booking).
     */

    @Transactional
    public BookingResponse createBooking(BookingRequest dto, Authentication authentication) {

        Long roomId = dto.getRoomId();
        Room room = roomService.getRoomById(roomId);  // validate room exists

        CustomUserPrincipal user =
                (CustomUserPrincipal) authentication.getPrincipal();

        Long userId = user.getId();

        validateBookingDates(dto.getCheckInDate(), dto.getCheckOutDate());

        checkOverlap(roomId, dto.getCheckInDate(), dto.getCheckOutDate(), null);

        Booking booking = new Booking();
        booking.setRoomId(roomId);
        booking.setUserId(userId);
        booking.setCheckInDate(dto.getCheckInDate());
        booking.setCheckOutDate(dto.getCheckOutDate());

        Booking savedBooking = bookingRepository.save(booking);

        // Send audit event
        Map<String, Object> payload = Map.of(
                "roomName", room.getName(),
                "checkInDate", dto.getCheckInDate(),
                "checkOutDate", dto.getCheckOutDate(),
                "price", room.getPrice()
        );

        AuditEventRequest auditEvent = new AuditEventRequest(
                UUID.randomUUID(),
                AuditEventType.BOOKING_CREATED,
                "booking-service",
                user.getUsername(),
                "BOOKING",
                savedBooking.getId(),
                payload,
                "Booking created successfully"
        );

        try {
            auditClient.sendAuditEvent(auditEvent);
        } catch (Exception e) {
            log.error("Failed to send audit event for booking {}", savedBooking.getId(), e);
        }

        return bookingMapper.toResponse(savedBooking);
    }

    // ================= UPDATE =================

    /**
     * Updates an existing booking.
     */
    @PreAuthorize("hasRole('ADMIN') or @bookingService.isOwner(#id,  authentication.principal.id)")
    @Transactional
    public BookingResponse updateBooking(Long id, BookingRequest dto, Authentication authentication) {

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Booking not found"
                ));

        Long roomId = dto.getRoomId();
        Room room = roomService.getRoomById(roomId);  // validate room exists

        validateBookingDates(dto.getCheckInDate(), dto.getCheckOutDate());

        checkOverlap(roomId, dto.getCheckInDate(), dto.getCheckOutDate(), booking.getId());

        booking.setRoomId(roomId);
        booking.setCheckInDate(dto.getCheckInDate());
        booking.setCheckOutDate(dto.getCheckOutDate());

        Booking updatedBooking = bookingRepository.save(booking);

        // Send audit event
        CustomUserPrincipal user =
                (CustomUserPrincipal) authentication.getPrincipal();

        Map<String, Object> payload = Map.of(
                "roomName", room.getName(),
                "checkInDate", dto.getCheckInDate(),
                "checkOutDate", dto.getCheckOutDate(),
                "price", room.getPrice()
        );

        AuditEventRequest auditEvent = new AuditEventRequest(
                UUID.randomUUID(),
                AuditEventType.BOOKING_UPDATED,
                "booking-service",
                user.getUsername(),
                "BOOKING",
                updatedBooking.getId(),
                payload,
                "Booking updated successfully"
        );

        try {
            auditClient.sendAuditEvent(auditEvent);
        } catch (Exception e) {
            log.error("Failed to send audit event for booking {}", updatedBooking.getId(), e);
        }

        return bookingMapper.toResponse(updatedBooking);
    }

    // ================= DELETE =================

    /**
     * Deletes a booking by id.
     * If booking does not exist, operation is silently ignored.
     */
    @PreAuthorize("hasRole('ADMIN') or @bookingService.isOwner(#id, authentication.principal.id)")
    @Transactional
    public void deleteBooking(Long id, Authentication authentication) {

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Booking not found"
                ));

        Room room = roomService.getRoomById(booking.getRoomId());

        CustomUserPrincipal user =
                (CustomUserPrincipal) authentication.getPrincipal();

        Map<String, Object> payload = Map.of(
                "roomName", room.getName(),
                "checkInDate", booking.getCheckInDate(),
                "checkOutDate", booking.getCheckOutDate(),
                "price", room.getPrice()
        );

        bookingRepository.delete(booking);

        AuditEventRequest auditEvent = new AuditEventRequest(
                UUID.randomUUID(),
                AuditEventType.BOOKING_CANCELLED,
                "booking-service",
                user.getUsername(),
                "BOOKING",
                booking.getId(),
                payload,
                "Booking cancelled successfully"
        );

        try {
            auditClient.sendAuditEvent(auditEvent);
        } catch (Exception e) {
            log.error("Failed to send audit event for booking {}", booking.getId(), e);
        }
    }

    // ============================================================
    // ================= HELPER METHODS (PRIVATE) =================
    // ============================================================

    /**
     * Helper method to check ownership
     */
    public boolean isOwner(Long bookingId, Long userId) {

        return bookingRepository.findById(bookingId)
                .map(b -> b.getUserId().equals(userId))
                .orElse(false);
    }

    /**
     * Validates booking dates (business rules).
     */
    private void validateBookingDates(LocalDate checkIn, LocalDate checkOut) {

        // Dates must not be null
        if (checkIn == null || checkOut == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Check-in and check-out dates are required"
            );
        }

        // Check-out must be after check-in
        if (!checkOut.isAfter(checkIn)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Check-out date must be after check-in date"
            );
        }

        // Booking cannot be more than 1 year in advance
        LocalDate maxAllowed = LocalDate.now().plusYears(1);
        if (checkIn.isAfter(maxAllowed) || checkOut.isAfter(maxAllowed)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Booking cannot be more than 1 year in advance"
            );
        }

        // Booking cannot start in the past
        if (checkIn.isBefore(LocalDate.now())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Booking cannot start in the past"
            );
        }
    }

    /**
     * Checks if there is an overlapping booking for the same room.
     * excludeBookingId is used when updating an existing booking.
     */
    private void checkOverlap(Long roomId, LocalDate checkIn, LocalDate checkOut, Long excludeBookingId) {

        boolean exists;

        if (excludeBookingId == null) {
            // CREATE case
            exists = bookingRepository.existsByRoomAndDateOverlap(roomId, checkIn, checkOut);
        } else {
            // UPDATE case
            exists = bookingRepository.existsByRoomAndDateOverlapExcludingBooking(
                    roomId,
                    checkIn,
                    checkOut,
                    excludeBookingId
            );
        }

        if (exists) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Room already booked for these dates"
            );
        }
    }
}
