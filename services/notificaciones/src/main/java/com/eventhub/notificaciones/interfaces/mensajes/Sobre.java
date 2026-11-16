package com.eventhub.notificaciones.interfaces.mensajes;

import java.time.Instant;
import java.util.UUID;

/**
 * Formato común de todos los mensajes (docs/contratos/README.md, "Formato común").
 * Los campos que no conocemos se ignoran: así un campo opcional nuevo no rompe el consumidor.
 */
public record Sobre<T>(UUID messageId, String tipo, Integer version, Instant fechaEmision,
                       String origen, T datos) {
}
