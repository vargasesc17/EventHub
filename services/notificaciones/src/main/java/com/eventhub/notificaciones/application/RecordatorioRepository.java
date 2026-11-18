package com.eventhub.notificaciones.application;

import com.eventhub.notificaciones.domain.Recordatorio;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Recordatorios de 24 horas (RF-04). */
public interface RecordatorioRepository {

    /**
     * Compradores de eventos NO cancelados que empiezan entre desde y hasta, y que todavía no
     * recibieron el recordatorio para la fecha ACTUAL del evento.
     */
    List<Recordatorio> pendientes(Instant desde, Instant hasta);

    void marcarEnviado(UUID boletaId, Instant fechaInicioEvento, Instant enviadoEn);
}
