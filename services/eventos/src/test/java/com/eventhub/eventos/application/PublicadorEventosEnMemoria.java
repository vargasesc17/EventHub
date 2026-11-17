package com.eventhub.eventos.application;

import com.eventhub.eventos.domain.Evento;
import com.eventhub.eventos.domain.TipoCambio;
import com.eventhub.eventos.domain.ValoresAnteriores;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class PublicadorEventosEnMemoria implements PublicadorEventos {

    public record Publicacion(Evento evento, Set<TipoCambio> cambios, ValoresAnteriores anteriores) { }

    private final List<Publicacion> publicaciones = new ArrayList<>();

    @Override
    public void eventoActualizado(Evento evento, Set<TipoCambio> cambios, ValoresAnteriores anteriores) {
        publicaciones.add(new Publicacion(evento, Set.copyOf(cambios), anteriores));
    }

    public List<Publicacion> publicaciones() {
        return List.copyOf(publicaciones);
    }

    public void limpiar() {
        publicaciones.clear();
    }
}