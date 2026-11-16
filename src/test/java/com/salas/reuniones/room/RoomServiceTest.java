package com.salas.reuniones.room;

import com.salas.reuniones.common.ConflictException;
import com.salas.reuniones.room.dto.CreateRoomRequest;
import com.salas.reuniones.room.dto.RoomResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Test unitario del servicio de salas: el repositorio está simulado con Mockito. */
@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @InjectMocks
    private RoomService roomService;

    @Test
    void creaLaSalaCuandoNoExisteNombreRepetido() {
        when(roomRepository.existsByNameIgnoreCase("Sala Aurora")).thenReturn(false);
        when(roomRepository.save(any(Room.class))).thenAnswer(inv -> inv.getArgument(0));

        RoomResponse response = roomService.create(new CreateRoomRequest("  Sala Aurora  ", 8));

        assertThat(response.name()).isEqualTo("Sala Aurora");
        assertThat(response.capacity()).isEqualTo(8);
    }

    @Test
    void rechazaNombreDuplicadoSinGuardar() {
        when(roomRepository.existsByNameIgnoreCase("sala aurora")).thenReturn(true);

        assertThatThrownBy(() -> roomService.create(new CreateRoomRequest("sala aurora", 8)))
                .isInstanceOf(ConflictException.class);

        verify(roomRepository, never()).save(any());
    }
}
