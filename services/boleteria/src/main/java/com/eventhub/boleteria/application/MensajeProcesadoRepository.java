package com.eventhub.boleteria.application;

import java.time.Instant;
import java.util.UUID;

/** Mensajes ya procesados. RabbitMQ puede entregar un mensaje dos veces; así la segunda se ignora. */
public interface MensajeProcesadoRepository {

    boolean yaProcesado(UUID messageId);

    void registrar(UUID messageId, String tipo, Instant procesadoEn);
}