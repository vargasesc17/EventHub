package com.eventhub.notificaciones.domain;

import java.time.Instant;

/**
 * Copia local de un evento en notificaciones_db (ADR-0006). La usan los recordatorios y las
 * alertas de cambio (#26); aquí se crea con los datos que trae la compra.
 *
 * @param info               nombre, fecha y lugar actuales
 * @param cancelado          si el evento fue cancelado (ya no se envían recordatorios)
 * @param ultimaFechaEmision fechaEmision del último evento.actualizado aplicado, o null si los
 *                           datos solo vienen de una boleta.comprada (#26 la usa para ignorar
 *                           mensajes viejos que lleguen fuera de orden)
 */
public record EventoLocal(InfoEvento info, boolean cancelado, Instant ultimaFechaEmision) {

    public EventoLocal {
        if (info == null) {
            throw new IllegalArgumentException("El evento local necesita sus datos");
        }
    }

    /** Un evento que se conoce por primera vez a través de una compra. */
    public static EventoLocal desdeCompra(InfoEvento info) {
        return new EventoLocal(info, false, null);
    }
}
