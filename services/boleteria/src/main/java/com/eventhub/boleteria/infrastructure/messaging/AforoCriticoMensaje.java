package com.eventhub.boleteria.infrastructure.messaging;

import com.eventhub.boleteria.domain.AforoEvento;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/** evento.aforo.critico tal como lo define docs/contratos. */
public record AforoCriticoMensaje(UUID messageId, String tipo, int version, Instant fechaEmision,
                                  String origen, Datos datos) {

    public static final String TIPO = "evento.aforo.critico";

    public record Datos(UUID eventoId, String nombreEvento, Organizador organizador, int aforoMaximo,
                        int vendidas, double porcentajeVendido, int umbral) { }

    public record Organizador(UUID organizadorId, String correo) { }

    public static AforoCriticoMensaje desde(AforoEvento evento, int umbral) {
        // Con un decimal: 451 de 500 → 90.2
        double porcentaje = Math.round(evento.vendidas() * 1000.0 / evento.aforoMaximo()) / 10.0;
        var datos = new Datos(evento.eventoId(), evento.nombre(),
                new Organizador(evento.organizadorId(), evento.organizadorCorreo()),
                evento.aforoMaximo(), evento.vendidas(), porcentaje, umbral);
        return new AforoCriticoMensaje(UUID.randomUUID(), TIPO, 1,
                Instant.now().truncatedTo(ChronoUnit.SECONDS), "boleteria", datos);
    }
}