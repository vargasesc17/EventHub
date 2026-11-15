package com.eventhub.eventos.application;

import com.eventhub.eventos.domain.Evento;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class ConsultarEventosUseCase {

    private final EventoRepository repositorio;

    public ConsultarEventosUseCase(EventoRepository repositorio) {
        this.repositorio = repositorio;
    }

    public Evento buscarPorId(UUID id) {
        return repositorio.buscarPorId(id)
                .orElseThrow(() -> new EventoNoEncontradoException(id));
    }

    public List<Evento> catalogo() {
        return repositorio.buscarPublicadosDesde(Instant.now());
    }
}