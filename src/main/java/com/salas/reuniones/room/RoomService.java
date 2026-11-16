package com.salas.reuniones.room;

import com.salas.reuniones.common.ConflictException;
import com.salas.reuniones.room.dto.CreateRoomRequest;
import com.salas.reuniones.room.dto.RoomResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RoomService {

    private final RoomRepository roomRepository;

    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @Transactional
    public RoomResponse create(CreateRoomRequest request) {
        String name = request.name().trim();
        if (roomRepository.existsByNameIgnoreCase(name)) {
            throw new ConflictException("Ya existe una sala llamada '" + name + "'");
        }
        Room room = roomRepository.save(new Room(name, request.capacity()));
        return RoomResponse.from(room);
    }

    @Transactional(readOnly = true)
    public List<RoomResponse> findAll() {
        return roomRepository.findAll().stream()
                .map(RoomResponse::from)
                .toList();
    }
}
