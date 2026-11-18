package com.eventhub.notificaciones.application;

import java.time.Instant;
import java.util.UUID;

/**
 * Alertas de aforo ya enviadas. Boletería publica la alerta una sola vez por evento (ADR-0003);
 * esto es una protección extra por si llegara repetida con otro messageId.
 */
public interface AlertaAforoRepository {

    /** Devuelve true si es la primera alerta de ese evento (y la deja registrada). */
    boolean registrarSiEsPrimera(UUID eventoId, Instant enviadaEn);
}
