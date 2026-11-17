package com.eventhub.notificaciones.application;

import java.time.Instant;
import java.util.UUID;

/**
 * Registro de mensajes ya procesados. RabbitMQ puede entregar un mensaje dos veces; gracias a
 * este registro la segunda vez se ignora (idempotencia). Así nadie recibe dos correos iguales.
 */
public interface MensajeProcesadoRepository {

    boolean yaProcesado(UUID messageId);

    void registrar(UUID messageId, String tipo, Instant procesadoEn);
}
