package com.eventhub.notificaciones.application;

import com.eventhub.notificaciones.domain.InfoEvento;
import com.eventhub.notificaciones.domain.TipoCambio;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * Lo que el caso de uso necesita de un evento.actualizado (docs/contratos).
 *
 * @param evento        nombre, fecha y lugar NUEVOS del evento
 * @param fechaAnterior fecha antes del cambio, o null si la fecha no cambió
 * @param lugarAnterior lugar antes del cambio, o null si el lugar no cambió
 */
public record EventoActualizadoComando(UUID messageId, Instant fechaEmision, Set<TipoCambio> cambios,
                                       InfoEvento evento, Instant fechaAnterior, String lugarAnterior) {

    public boolean incluye(TipoCambio cambio) {
        return cambios.contains(cambio);
    }
}
