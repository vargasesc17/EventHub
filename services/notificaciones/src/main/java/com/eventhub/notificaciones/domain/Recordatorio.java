package com.eventhub.notificaciones.domain;

import java.util.UUID;

/** Un recordatorio de 24 horas pendiente de enviar a un comprador (RF-04). */
public record Recordatorio(UUID boletaId, String nombre, String correo, InfoEvento evento) {

    public Recordatorio {
        if (boletaId == null || nombre == null || correo == null || evento == null) {
            throw new IllegalArgumentException("Al recordatorio le faltan datos");
        }
    }
}
