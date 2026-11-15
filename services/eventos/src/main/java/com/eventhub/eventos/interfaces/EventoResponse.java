package com.eventhub.eventos.interfaces;

import com.eventhub.eventos.domain.EstadoEvento;
import com.eventhub.eventos.domain.Evento;
import com.eventhub.eventos.domain.TipoBoleta;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record EventoResponse(UUID id, String nombre, String lugar, Instant fechaInicio, int aforoMaximo,
                             UUID organizadorId, String organizadorCorreo, EstadoEvento estado,
                             List<CategoriaResponse> categorias) {

    public record CategoriaResponse(TipoBoleta tipo, int cupos, long precioBase) { }

    static EventoResponse desde(Evento evento) {
        var categorias = evento.getCategorias().stream()
                .map(c -> new CategoriaResponse(c.tipo(), c.cupos(), c.precioBase()))
                .toList();
        return new EventoResponse(evento.getId(), evento.getNombre(), evento.getLugar(),
                evento.getFechaInicio(), evento.getAforoMaximo(), evento.getOrganizadorId(),
                evento.getOrganizadorCorreo(), evento.getEstado(), categorias);
    }
}