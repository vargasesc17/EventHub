package com.eventhub.boleteria.interfaces.mensajes;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Campo "datos" de evento.actualizado (docs/contratos/evento.actualizado.schema.json). */
public record DatosEventoActualizado(UUID eventoId, List<String> cambios, String estado, String nombre,
                                     Instant fechaInicio, String lugar, Integer aforoMaximo,
                                     Organizador organizador, List<Categoria> categorias) {

    public record Organizador(UUID organizadorId, String correo) { }

    public record Categoria(String categoria, Integer cupos, Long precioBase) { }
}