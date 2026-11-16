package com.salas.reuniones.booking;

import com.salas.reuniones.room.Room;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(name = "responsible", nullable = false, length = 100)
    private String responsible;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime start;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime end;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookingStatus status;

    protected Booking() {
        // requerido por JPA
    }

    public Booking(Room room, String responsible, LocalDateTime start, LocalDateTime end) {
        this.room = room;
        this.responsible = responsible;
        this.start = start;
        this.end = end;
        this.status = BookingStatus.ACTIVE;
    }

    /** Dos intervalos se solapan si empiezan antes de que acabe el otro (extremos abiertos). */
    public boolean overlaps(LocalDateTime otherStart, LocalDateTime otherEnd) {
        return start.isBefore(otherEnd) && otherStart.isBefore(end);
    }

    public void cancel() {
        this.status = BookingStatus.CANCELLED;
    }

    public boolean isActive() {
        return status == BookingStatus.ACTIVE;
    }

    public Long getId() {
        return id;
    }

    public Room getRoom() {
        return room;
    }

    public String getResponsible() {
        return responsible;
    }

    public LocalDateTime getStart() {
        return start;
    }

    public LocalDateTime getEnd() {
        return end;
    }

    public BookingStatus getStatus() {
        return status;
    }
}
