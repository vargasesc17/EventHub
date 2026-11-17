package com.eventhub.notificaciones.application;

import com.eventhub.notificaciones.domain.InfoEvento;

import java.util.UUID;

/**
 * Lo que el caso de uso necesita de un mensaje boleta.comprada (docs/contratos).
 * La capa interfaces lo arma a partir del JSON; así el caso de uso no sabe nada de JSON.
 */
public record BoletaCompradaComando(
        UUID messageId,
        UUID boletaId,
        String categoria,
        String codigoQr,
        UUID compradorId,
        String nombreComprador,
        String correoComprador,
        InfoEvento evento) {
}
