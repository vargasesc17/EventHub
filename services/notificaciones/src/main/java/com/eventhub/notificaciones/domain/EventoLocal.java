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

    /**
     * ¿Ya se aplicó un evento.actualizado igual o más reciente que uno emitido en fechaEmision?
     * Si es así, el mensaje llegó fuera de orden y se ignora (docs/contratos, "Orden").
     */
    public boolean yaAplicoUnoPosteriorA(Instant fechaEmision) {
        return ultimaFechaEmision != null && !fechaEmision.isAfter(ultimaFechaEmision);
    }

    /** Un evento que se conoce por primera vez a través de una compra. */
    public static EventoLocal desdeCompra(InfoEvento info) {
        return new EventoLocal(info, false, null);
    }
}
