package com.eventhub.eventos.infrastructure;

import com.eventhub.eventos.domain.CategoriaBoleta;
import com.eventhub.eventos.domain.Evento;

final class EventoMapper {

    private EventoMapper() { }

    static EventoEntity aEntidad(Evento evento) {
        var categorias = evento.getCategorias().stream()
                .map(c -> new CategoriaBoletaEmbeddable(c.tipo(), c.cupos(), c.precioBase()))
                .toList();
        return new EventoEntity(evento.getId(), evento.getNombre(), evento.getLugar(),
                evento.getFechaInicio(), evento.getAforoMaximo(), evento.getOrganizadorId(),
                evento.getOrganizadorCorreo(), evento.getEstado(), categorias);
    }

    static Evento aDominio(EventoEntity entidad) {
        var categorias = entidad.getCategorias().stream()
                .map(c -> new CategoriaBoleta(c.getTipo(), c.getCupos(), c.getPrecioBase()))
                .toList();
        return Evento.reconstruir(entidad.getId(), entidad.getNombre(), entidad.getLugar(),
                entidad.getFechaInicio(), entidad.getAforoMaximo(), entidad.getOrganizadorId(),
                entidad.getOrganizadorCorreo(), entidad.getEstado(), categorias);
    }
}