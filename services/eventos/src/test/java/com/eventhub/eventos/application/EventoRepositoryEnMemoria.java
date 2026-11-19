package com.eventhub.eventos.application;

import com.eventhub.eventos.domain.EstadoEvento;
import com.eventhub.eventos.domain.Evento;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

class EventoRepositoryEnMemoria implements EventoRepository {

    private final Map<UUID, Evento> eventos = new HashMap<>();

    @Override
    public void guardar(Evento evento) {
        eventos.put(evento.getId(), evento);
    }

    @Override
    public Optional<Evento> buscarPorId(UUID id) {
        return Optional.ofNullable(eventos.get(id));
    }

    @Override
    public List<Evento> buscarPublicadosDesde(Instant ahora) {
        return eventos.values().stream()
                .filter(e -> e.getEstado() == EstadoEvento.PUBLICADO)
                .filter(e -> e.getFechaInicio().isAfter(ahora))
                .toList();
    }

    @Override
    public List<Evento> buscarPorOrganizador(UUID organizadorId) {
        return eventos.values().stream()
                .filter(e -> e.getOrganizadorId().equals(organizadorId))
                .sorted(java.util.Comparator.comparing(Evento::getFechaInicio))
                .toList();
    }

    int cantidad() {
        return eventos.size();
    }
}