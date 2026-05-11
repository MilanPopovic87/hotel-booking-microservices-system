package com.hotel.booking.controller;

import com.hotel.booking.entity.Room;
import com.hotel.booking.service.RoomService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping
    public List<Room> getAllRooms() {
        return roomService.getAllRooms();
    }

    @GetMapping("/{id}")
    public Room getRoom(@PathVariable Long id) {
        return roomService.getRoomById(id);
    }

    @PostMapping
    public Room createRoom(@RequestBody Room room) {
        return roomService.saveRoom(room);
    }

    @PutMapping("/{id}")
    public Room updateRoom(@PathVariable Long id, @RequestBody Room room) {
        room.setId(id);
        return  roomService.saveRoom(room);
    }

    @DeleteMapping("/{id}")
    public void deleteRoom(@PathVariable Long id) {
        roomService.deleteRoom(id);
    }
}
