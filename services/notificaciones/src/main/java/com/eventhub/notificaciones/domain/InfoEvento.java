package com.eventhub.notificaciones.domain;

import java.time.Instant;
import java.util.UUID;

/** Datos de un evento que Notificaciones necesita para escribir los correos. */
public record InfoEvento(UUID eventoId, String nombre, Instant fechaInicio, String lugar) {

    public InfoEvento {
        if (eventoId == null || nombre == null || fechaInicio == null || lugar == null) {
            throw new IllegalArgumentException("Al evento le faltan datos (id, nombre, fecha o lugar)");
        }
    }
}
