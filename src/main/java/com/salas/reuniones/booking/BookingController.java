package com.salas.reuniones.booking;

import com.salas.reuniones.booking.dto.BookingResponse;
import com.salas.reuniones.booking.dto.CreateBookingRequest;
import com.salas.reuniones.common.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/bookings")
@Tag(name = "Reservas", description = "Crear, cancelar y consultar reservas de salas")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    @SecurityRequirement(name = "basicAuth")
    @Operation(summary = "Crear una reserva", description = "Devuelve 409 si la sala ya está ocupada en ese horario")
    @ApiResponse(responseCode = "201", description = "Reserva creada")
    @ApiResponse(responseCode = "400", description = "Datos no válidos")
    @ApiResponse(responseCode = "409", description = "Solapamiento con otra reserva activa")
    public ResponseEntity<BookingResponse> create(@Valid @RequestBody CreateBookingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.create(request));
    }

    @PostMapping("/{id}/cancel")
    @SecurityRequirement(name = "basicAuth")
    @Operation(summary = "Cancelar una reserva", description = "Cancelación lógica: la reserva queda en estado CANCELLED")
    @ApiResponse(responseCode = "409", description = "La reserva ya estaba cancelada")
    public BookingResponse cancel(@PathVariable Long id) {
        return bookingService.cancel(id);
    }

    /**
     * Listado paginado. Filtros opcionales: roomId y date (yyyy-MM-dd).
     * Ejemplo: GET /api/bookings?roomId=1&date=2030-01-10&page=0&size=20&sort=start,asc
     */
    @GetMapping
    @Operation(summary = "Listar reservas paginadas", description = "Filtros opcionales por sala y fecha")
    public PageResponse<BookingResponse> list(
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @PageableDefault(size = 20, sort = "start") Pageable pageable) {
        return bookingService.search(roomId, date, pageable);
    }
}
