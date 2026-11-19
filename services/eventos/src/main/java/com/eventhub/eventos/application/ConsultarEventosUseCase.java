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

    /** "Mis eventos" del organizador: incluye borradores, cancelados y finalizados. */
    public List<Evento> delOrganizador(UUID organizadorId) {
        if (organizadorId == null) {
            throw new IllegalArgumentException("La consulta exige el id del organizador");
        }
        return repositorio.buscarPorOrganizador(organizadorId);
    }

    public List<Evento> catalogo() {
        return repositorio.buscarPublicadosDesde(Instant.now());
    }
}