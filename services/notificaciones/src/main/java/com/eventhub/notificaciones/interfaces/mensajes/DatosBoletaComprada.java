package com.eventhub.notificaciones.interfaces.mensajes;

import java.time.Instant;
import java.util.UUID;

/** Campo "datos" de boleta.comprada (docs/contratos/boleta.comprada.schema.json). */
public record DatosBoletaComprada(
        UUID boletaId,
        UUID eventoId,
        String categoria,
        Long precioPagado,
        Instant fechaCompra,
        String codigoQr,
        Comprador comprador,
        Evento evento) {

    public record Comprador(UUID compradorId, String nombre, String correo) { }

    public record Evento(String nombre, Instant fechaInicio, String lugar) { }
}
