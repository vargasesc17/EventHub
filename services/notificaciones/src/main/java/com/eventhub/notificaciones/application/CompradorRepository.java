package com.eventhub.notificaciones.application;

import com.eventhub.notificaciones.domain.CompradorEvento;

import java.util.List;
import java.util.UUID;

/** Copia local de compradores por evento (ADR-0006). */
public interface CompradorRepository {

    /** Guarda el comprador; si esa boleta ya estaba guardada, no hace nada. */
    void guardarSiNoExiste(CompradorEvento comprador);

    List<CompradorEvento> buscarPorEvento(UUID eventoId);
}
