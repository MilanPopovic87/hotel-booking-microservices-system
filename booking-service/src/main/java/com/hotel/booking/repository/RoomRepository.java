package com.hotel.booking.repository;

import com.hotel.booking.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {

    Optional<Room> findByName(String name);

    long countByAvailableTrue(); // counts available rooms
}
