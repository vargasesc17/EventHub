package com.eventhub.eventos.application;

import com.eventhub.eventos.domain.CategoriaBoleta;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CrearEventoCommand(String nombre, String lugar, Instant fechaInicio, int aforoMaximo,
                                 UUID organizadorId, String organizadorCorreo,
                                 List<CategoriaBoleta> categorias) { }