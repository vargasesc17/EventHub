package com.eventhub.notificaciones.interfaces.mensajes;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Campo "datos" de evento.actualizado (docs/contratos/evento.actualizado.schema.json).
 * Solo se leen los campos que Notificaciones usa; el resto (aforo, categorías...) se ignora.
 */
public record DatosEventoActualizado(
        UUID eventoId,
        List<String> cambios,
        String estado,
        String nombre,
        Instant fechaInicio,
        String lugar,
        ValoresAnteriores valoresAnteriores) {

    public record ValoresAnteriores(Instant fechaInicio, String lugar) { }
}
