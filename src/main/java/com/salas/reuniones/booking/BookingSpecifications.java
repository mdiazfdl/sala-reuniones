package com.salas.reuniones.booking;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class BookingSpecifications {

    private BookingSpecifications() {
    }

    /**
     * Filtra por sala y/o por día. Una reserva "cae" en un día si se solapa con él,
     * así que una reserva que cruza medianoche aparece en ambos días.
     */
    public static Specification<Booking> filter(Long roomId, LocalDate date) {
        return (root, query, cb) -> {
            // Evita N+1 al leer el nombre de la sala; no aplica a la consulta de conteo.
            if (query.getResultType() != Long.class) {
                root.fetch("room");
            }

            List<Predicate> predicates = new ArrayList<>();
            if (roomId != null) {
                predicates.add(cb.equal(root.get("room").get("id"), roomId));
            }
            if (date != null) {
                var dayStart = date.atStartOfDay();
                var dayEnd = dayStart.plusDays(1);
                predicates.add(cb.lessThan(root.get("start"), dayEnd));
                predicates.add(cb.greaterThan(root.get("end"), dayStart));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
