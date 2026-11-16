package com.eventhub.eventos.domain;

import java.time.Instant;

/** Lo que tenía el evento antes de un cambio de fecha y/o lugar. Cada campo es null si no cambió. */
public record ValoresAnteriores(Instant fechaInicio, String lugar) { }