package com.hotel.booking.service;

import com.hotel.booking.dto.BookingRequest;
import com.hotel.booking.dto.BookingResponse;
import com.hotel.booking.entity.Booking;
import com.hotel.booking.mapper.BookingMapper;
import com.hotel.booking.repository.BookingRepository;
import com.hotel.booking.security.CustomUserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.Authentication;

import java.time.LocalDate;
import java.util.List;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;
    private final RoomService roomService;

    public BookingService(
            BookingRepository bookingRepository,
            BookingMapper bookingMapper,
            RoomService roomService
    ) {
        this.bookingRepository = bookingRepository;
        this.bookingMapper = bookingMapper;
        this.roomService = roomService;
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
        roomService.getRoomById(roomId);  // validate room exists

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
        return bookingMapper.toResponse(savedBooking);
    }

    // ================= UPDATE =================

    /**
     * Updates an existing booking.
     */
    @PreAuthorize("hasRole('ADMIN') or @bookingService.isOwner(#id,  authentication.principal.id)")
    @Transactional
    public BookingResponse updateBooking(Long id, BookingRequest dto) {

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Booking not found"
                ));

        Long roomId = dto.getRoomId();
        roomService.getRoomById(roomId);  // validate room exists

        validateBookingDates(dto.getCheckInDate(), dto.getCheckOutDate());

        checkOverlap(roomId, dto.getCheckInDate(), dto.getCheckOutDate(), booking.getId());

        booking.setRoomId(roomId);
        booking.setCheckInDate(dto.getCheckInDate());
        booking.setCheckOutDate(dto.getCheckOutDate());

        Booking updatedBooking = bookingRepository.save(booking);
        return bookingMapper.toResponse(updatedBooking);
    }

    // ================= DELETE =================

    /**
     * Deletes a booking by id.
     * If booking does not exist, operation is silently ignored.
     */
    @PreAuthorize("hasRole('ADMIN') or @bookingService.isOwner(#id, authentication.principal.id)")
    public void deleteBooking(Long id) {
        bookingRepository.deleteById(id);
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
