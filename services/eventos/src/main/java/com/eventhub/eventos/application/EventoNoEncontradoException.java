package com.eventhub.eventos.application;

import java.util.UUID;

public class EventoNoEncontradoException extends RuntimeException {
    public EventoNoEncontradoException(UUID id) {
        super("No existe un evento con id " + id);
    }
}