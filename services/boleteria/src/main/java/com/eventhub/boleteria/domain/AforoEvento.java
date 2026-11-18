package com.eventhub.boleteria.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * Copia local de un evento en Boletería: lo necesario para vender (aforo, si acepta ventas),
 * para la alerta del 90 % (organizador) y para el mensaje boleta.comprada (nombre, fecha, lugar).
 */
public record AforoEvento(UUID eventoId, String nombre, Instant fechaInicio, String lugar,
                          UUID organizadorId, String organizadorCorreo, int aforoMaximo, int vendidas,
                          boolean alertaEnviada, boolean ventasBloqueadas) {

    public AforoEvento {
        if (eventoId == null || nombre == null || nombre.isBlank() || fechaInicio == null
                || lugar == null || lugar.isBlank() || organizadorId == null || organizadorCorreo == null) {
            throw new ReglaDeNegocioException("A la copia del evento le faltan datos obligatorios");
        }
        if (aforoMaximo <= 0) {
            throw new ReglaDeNegocioException("El aforo máximo debe ser mayor que 0 (recibido: " + aforoMaximo + ")");
        }
        if (vendidas < 0 || vendidas > aforoMaximo) {
            throw new ReglaDeNegocioException("Las vendidas deben estar entre 0 y " + aforoMaximo
                    + " (recibido: " + vendidas + ")");
        }
    }

    /** Un evento recién publicado: sin ventas, sin alerta y aceptando compras. */
    public static AforoEvento nuevo(UUID eventoId, String nombre, Instant fechaInicio, String lugar,
                                    UUID organizadorId, String organizadorCorreo, int aforoMaximo) {
        return new AforoEvento(eventoId, nombre, fechaInicio, lugar, organizadorId, organizadorCorreo,
                aforoMaximo, 0, false, false);
    }

    /** El mismo >= del ADR-0003: se alcanza el umbral al llegar a él o superarlo. */
    public boolean alcanzaUmbral(int umbralPorcentaje) {
        return (long) vendidas * 100 >= (long) aforoMaximo * umbralPorcentaje;
    }

    public boolean aceptaVentas() {
        return !ventasBloqueadas;
    }
}