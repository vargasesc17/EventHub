package com.eventhub.notificaciones.interfaces.mensajes;

import java.util.UUID;

/** Campo "datos" de evento.aforo.critico (docs/contratos/evento.aforo.critico.schema.json). */
public record DatosAforoCritico(
        UUID eventoId,
        String nombreEvento,
        Organizador organizador,
        Integer aforoMaximo,
        Integer vendidas,
        Double porcentajeVendido,
        Integer umbral) {

    public record Organizador(UUID organizadorId, String correo) { }
}
