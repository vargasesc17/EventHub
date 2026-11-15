package com.eventhub.eventos.application;

import com.eventhub.eventos.domain.CategoriaBoleta;

import java.time.Instant;
import java.util.List;

public record EditarEventoCommand(String nombre, String lugar, Instant fechaInicio, int aforoMaximo,
                                  List<CategoriaBoleta> categorias) { }