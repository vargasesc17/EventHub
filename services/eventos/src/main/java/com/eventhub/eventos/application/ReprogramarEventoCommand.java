package com.eventhub.eventos.application;

import java.time.Instant;

/** Cambio de fecha y/o lugar de un evento publicado. El campo que no cambia va en null. */
public record ReprogramarEventoCommand(Instant fechaInicio, String lugar) { }