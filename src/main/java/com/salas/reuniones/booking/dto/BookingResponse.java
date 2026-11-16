package com.salas.reuniones.booking.dto;

import com.salas.reuniones.booking.Booking;
import com.salas.reuniones.booking.BookingStatus;

import java.time.LocalDateTime;

public record BookingResponse(
        Long id,
        Long roomId,
        String roomName,
        String responsible,
        LocalDateTime start,
        LocalDateTime end,
        BookingStatus status
) {

    public static BookingResponse from(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getRoom().getId(),
                booking.getRoom().getName(),
                booking.getResponsible(),
                booking.getStart(),
                booking.getEnd(),
                booking.getStatus());
    }
}
