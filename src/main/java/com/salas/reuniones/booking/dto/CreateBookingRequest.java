package com.salas.reuniones.booking.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record CreateBookingRequest(
        @NotNull(message = "La sala es obligatoria")
        Long roomId,

        @NotBlank(message = "El responsable es obligatorio")
        @Size(max = 100, message = "El responsable no puede superar 100 caracteres")
        String responsible,

        @NotNull(message = "La hora de inicio es obligatoria")
        @FutureOrPresent(message = "La reserva no puede empezar en el pasado")
        LocalDateTime start,

        @NotNull(message = "La hora de fin es obligatoria")
        LocalDateTime end
) {

    @AssertTrue(message = "La hora de fin debe ser posterior a la de inicio")
    public boolean isEndAfterStart() {
        return start == null || end == null || end.isAfter(start);
    }
}
