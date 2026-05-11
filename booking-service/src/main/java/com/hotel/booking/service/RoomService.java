package com.hotel.booking.service;

import com.hotel.booking.entity.Room;
import com.hotel.booking.repository.BookingRepository;
import com.hotel.booking.repository.RoomRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;

    public RoomService(RoomRepository roomRepository, BookingRepository bookingRepository) {
        this.roomRepository = roomRepository;
        this.bookingRepository = bookingRepository;
    }

    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    public Room getRoomById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Room not found"
                ));
    }

    public long countAvailableRooms() {
        return roomRepository.countByAvailableTrue();
    }

    public Room saveRoom(Room room) {
        // Find room with the same name
        Optional<Room> existingRoom = roomRepository.findByName(room.getName());

        if (existingRoom.isPresent() && !existingRoom.get().getId().equals(room.getId())) {
            // Another room with same name exists → conflict
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Room name already exists"
            );
        }

        return roomRepository.save(room);
    }

    public void deleteRoom(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found"));

        if (bookingRepository.existsByRoomId(id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Room has bookings and cannot be deleted");
        }

        roomRepository.delete(room);
    }
}
