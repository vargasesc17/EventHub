package com.eventhub.notificaciones.infrastructure.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface EventoLocalJpaRepository extends JpaRepository<EventoLocalEntity, UUID> {

    /** Eventos no cancelados que empiezan entre desde y hasta (para los recordatorios). */
    List<EventoLocalEntity> findByCanceladoFalseAndFechaInicioBetween(Instant desde, Instant hasta);
}
