package com.salas.reuniones.room.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateRoomRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
        String name,

        @Min(value = 1, message = "La capacidad debe ser al menos 1")
        @Max(value = 1000, message = "La capacidad no puede superar 1000")
        int capacity
) {
}
