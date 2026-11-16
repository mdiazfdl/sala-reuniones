package com.salas.reuniones.room.dto;

import com.salas.reuniones.room.Room;

public record RoomResponse(Long id, String name, int capacity) {

    public static RoomResponse from(Room room) {
        return new RoomResponse(room.getId(), room.getName(), room.getCapacity());
    }
}
