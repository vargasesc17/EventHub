package com.eventhub.boleteria.application;

import com.eventhub.boleteria.domain.AforoEvento;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/** Copia local de los eventos. Cada cambio toca solo sus columnas, para no pisar los contadores de venta. */
public interface AforoRepository {

    boolean existe(UUID eventoId);

    void crear(AforoEvento aforo);

    Optional<AforoEvento> buscar(UUID eventoId);

    void actualizarProgramacion(UUID eventoId, Instant fechaInicio, String lugar);

    void bloquearVentas(UUID eventoId);
}