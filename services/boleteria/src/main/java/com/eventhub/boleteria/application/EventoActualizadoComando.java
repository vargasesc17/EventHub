package com.eventhub.boleteria.application;

import com.eventhub.boleteria.domain.TipoBoleta;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Lo que el caso de uso necesita de un mensaje evento.actualizado. La capa interfaces lo arma desde el JSON. */
public record EventoActualizadoComando(UUID messageId, UUID eventoId, Set<TipoCambio> cambios, String nombre,
                                       Instant fechaInicio, String lugar, int aforoMaximo,
                                       UUID organizadorId, String organizadorCorreo,
                                       List<Categoria> categorias) {

    public record Categoria(TipoBoleta categoria, int cupos, long precioBase) { }
}