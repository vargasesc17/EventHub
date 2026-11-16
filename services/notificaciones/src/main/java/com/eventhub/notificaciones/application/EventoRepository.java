package com.eventhub.notificaciones.application;

import com.eventhub.notificaciones.domain.EventoLocal;

import java.util.Optional;
import java.util.UUID;

/** Copia local de los eventos (nombre, fecha, lugar) para escribir los correos (ADR-0006). */
public interface EventoRepository {

    Optional<EventoLocal> buscar(UUID eventoId);

    /** Inserta el evento o reemplaza sus datos si ya existía. */
    void guardar(EventoLocal evento);
}
