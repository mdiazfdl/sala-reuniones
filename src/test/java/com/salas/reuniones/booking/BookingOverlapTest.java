package com.salas.reuniones.booking;

import com.salas.reuniones.room.Room;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/** Test unitario: la regla de solapamiento de intervalos [inicio, fin), sin Spring. */
class BookingOverlapTest {

    private final Booking existing = new Booking(
            new Room("Sala Test", 4),
            "Ana",
            LocalDateTime.of(2030, 1, 10, 9, 0),
            LocalDateTime.of(2030, 1, 10, 10, 0));

    @Test
    void detectaSolapamientoParcial() {
        assertThat(existing.overlaps(
                LocalDateTime.of(2030, 1, 10, 9, 30),
                LocalDateTime.of(2030, 1, 10, 11, 0))).isTrue();
    }

    @Test
    void detectaIntervaloQueContieneAlExistente() {
        assertThat(existing.overlaps(
                LocalDateTime.of(2030, 1, 10, 8, 0),
                LocalDateTime.of(2030, 1, 10, 12, 0))).isTrue();
    }

    @Test
    void noConsideraSolapamientoUnaReservaContigua() {
        // Una reserva que empieza justo cuando acaba la otra no solapa.
        assertThat(existing.overlaps(
                LocalDateTime.of(2030, 1, 10, 10, 0),
                LocalDateTime.of(2030, 1, 10, 11, 0))).isFalse();
        assertThat(existing.overlaps(
                LocalDateTime.of(2030, 1, 10, 8, 0),
                LocalDateTime.of(2030, 1, 10, 9, 0))).isFalse();
    }
}
