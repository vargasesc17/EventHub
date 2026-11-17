package com.eventhub.eventos.infrastructure;

import com.eventhub.eventos.domain.EstadoEvento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface EventoJpaRepository extends JpaRepository<EventoEntity, UUID> {
    List<EventoEntity> findByEstadoAndFechaInicioAfter(EstadoEvento estado, Instant fecha);
}