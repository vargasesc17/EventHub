package com.eventhub.eventos.application;

import com.eventhub.eventos.domain.Evento;

import java.util.UUID;

public class GestionarEventoUseCase {

    private final EventoRepository repositorio;

    public GestionarEventoUseCase(EventoRepository repositorio) {
        this.repositorio = repositorio;
    }

    public Evento crear(CrearEventoCommand datos) {
        var evento = Evento.crear(datos.nombre(), datos.lugar(), datos.fechaInicio(), datos.aforoMaximo(),
                datos.organizadorId(), datos.organizadorCorreo(), datos.categorias());
        repositorio.guardar(evento);
        return evento;
    }

    public Evento editar(UUID id, EditarEventoCommand datos) {
        var evento = repositorio.buscarPorId(id)
                .orElseThrow(() -> new EventoNoEncontradoException(id));
        evento.editar(datos.nombre(), datos.lugar(), datos.fechaInicio(), datos.aforoMaximo(),
                datos.categorias());
        repositorio.guardar(evento);
        return evento;
    }
}