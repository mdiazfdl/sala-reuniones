package com.salas.reuniones.booking;

import com.salas.reuniones.common.ConflictException;
import com.salas.reuniones.common.NotFoundException;
import com.salas.reuniones.booking.dto.BookingResponse;
import com.salas.reuniones.booking.dto.CreateBookingRequest;
import com.salas.reuniones.room.Room;
import com.salas.reuniones.room.RoomRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;

    public BookingService(BookingRepository bookingRepository, RoomRepository roomRepository) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
    }

    @Transactional
    public BookingResponse create(CreateBookingRequest request) {
        // Bloquea la sala: otra reserva concurrente de la misma sala esperará aquí.
        Room room = roomRepository.findByIdForUpdate(request.roomId())
                .orElseThrow(() -> new NotFoundException("No existe la sala " + request.roomId()));

        boolean occupied = bookingRepository.existsByRoomIdAndStatusAndStartLessThanAndEndGreaterThan(
                room.getId(), BookingStatus.ACTIVE, request.end(), request.start());
        if (occupied) {
            throw new ConflictException("La sala '" + room.getName() + "' ya está reservada en ese horario");
        }

        Booking booking = new Booking(room, request.responsible().trim(), request.start(), request.end());
        return BookingResponse.from(bookingRepository.save(booking));
    }

    @Transactional
    public BookingResponse cancel(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("No existe la reserva " + id));
        if (!booking.isActive()) {
            throw new ConflictException("La reserva " + id + " ya está cancelada");
        }
        booking.cancel();
        return BookingResponse.from(booking);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> findAll() {
        return bookingRepository.findAll(Sort.by("start")).stream()
                .map(BookingResponse::from)
                .toList();
    }
}
