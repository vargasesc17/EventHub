package com.eventhub.eventos.interfaces;

import com.eventhub.eventos.application.ReprogramarEventoCommand;

import java.time.Instant;

/** Cambio de fecha y/o lugar. El campo que no cambia se omite. */
public record ReprogramarEventoRequest(Instant fechaInicio, String lugar) {

    ReprogramarEventoCommand aCommand() {
        return new ReprogramarEventoCommand(fechaInicio, lugar);
    }
}