package com.salas.reuniones.room;

import com.salas.reuniones.room.dto.CreateRoomRequest;
import com.salas.reuniones.room.dto.RoomResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@Tag(name = "Salas", description = "Alta y consulta de salas de reuniones")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @PostMapping
    @SecurityRequirement(name = "basicAuth")
    @Operation(summary = "Crear una sala")
    public ResponseEntity<RoomResponse> create(@Valid @RequestBody CreateRoomRequest request) {
        RoomResponse created = roomService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    @Operation(summary = "Listar salas")
    public List<RoomResponse> list() {
        return roomService.findAll();
    }
}
