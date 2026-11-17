package com.eventhub.boleteria.application;

import java.util.UUID;

/** El evento no existe en Boletería (no se ha publicado). Se responde 404. */
public class EventoNoEncontradoException extends RuntimeException {
    public EventoNoEncontradoException(UUID eventoId) {
        super("No hay un evento a la venta con id " + eventoId);
    }
}