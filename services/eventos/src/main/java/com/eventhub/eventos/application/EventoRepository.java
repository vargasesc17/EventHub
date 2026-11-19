package com.eventhub.eventos.application;

import com.eventhub.eventos.domain.Evento;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EventoRepository {
    void guardar(Evento evento);
    Optional<Evento> buscarPorId(UUID id);
    /** Eventos PUBLICADOS cuya fecha de inicio es posterior a "ahora" (RF-06). */
    List<Evento> buscarPublicadosDesde(Instant ahora);

    /** Todos los eventos de un organizador, en cualquier estado, del más próximo al más lejano. */
    List<Evento> buscarPorOrganizador(UUID organizadorId);
}