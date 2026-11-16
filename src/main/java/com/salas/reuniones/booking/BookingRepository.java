package com.salas.reuniones.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;

public interface BookingRepository extends JpaRepository<Booking, Long>, JpaSpecificationExecutor<Booking> {

    /** Hay una reserva activa en la sala que solapa el intervalo [start, end). */
    boolean existsByRoomIdAndStatusAndStartLessThanAndEndGreaterThan(
            Long roomId, BookingStatus status, LocalDateTime end, LocalDateTime start);
}
