package com.eventhub.notificaciones.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * Un comprador de un evento, guardado en la copia local de Notificaciones (ADR-0006).
 * Con esta lista, Notificaciones puede avisarles a todos los compradores sin preguntarle a
 * Boletería. Se identifica por la boleta: una persona con dos boletas aparece dos veces.
 */
public record CompradorEvento(UUID boletaId, UUID eventoId, UUID compradorId,
                              String nombre, String correo, Instant registradoEn) {

    public CompradorEvento {
        if (boletaId == null || eventoId == null || compradorId == null
                || nombre == null || correo == null || registradoEn == null) {
            throw new IllegalArgumentException("Al comprador le faltan datos");
        }
    }
}
