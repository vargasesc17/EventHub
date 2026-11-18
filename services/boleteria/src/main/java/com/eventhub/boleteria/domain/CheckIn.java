package com.eventhub.boleteria.domain;

import java.time.Instant;
import java.util.UUID;

/** Registro de un ingreso al evento: qué boleta, quién la validó y cuándo (RF-03, #24). */
public record CheckIn(UUID id, UUID boletaId, UUID eventoId, UUID staffId, Instant fecha) {

    public CheckIn {
        if (id == null || boletaId == null || eventoId == null || staffId == null || fecha == null) {
            throw new ReglaDeNegocioException("Al check-in le faltan datos (boleta, evento, staff o fecha)");
        }
    }
}
