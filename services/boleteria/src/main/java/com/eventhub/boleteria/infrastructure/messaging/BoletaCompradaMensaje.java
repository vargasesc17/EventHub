package com.eventhub.boleteria.infrastructure.messaging;

import com.eventhub.boleteria.domain.AforoEvento;
import com.eventhub.boleteria.domain.Boleta;
import com.eventhub.boleteria.domain.Compra;
import com.eventhub.boleteria.domain.TipoBoleta;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/** boleta.comprada tal como lo define docs/contratos. */
public record BoletaCompradaMensaje(UUID messageId, String tipo, int version, Instant fechaEmision,
                                    String origen, Datos datos) {

    public static final String TIPO = "boleta.comprada";

    public record Datos(UUID boletaId, UUID eventoId, TipoBoleta categoria, long precioPagado,
                        Instant fechaCompra, String codigoQr, Comprador comprador, Evento evento) { }

    public record Comprador(UUID compradorId, String nombre, String correo) { }

    public record Evento(String nombre, Instant fechaInicio, String lugar) { }

    public static BoletaCompradaMensaje desde(Boleta boleta, Compra compra, AforoEvento evento, String codigoQr) {
        var datos = new Datos(boleta.getId(), boleta.getEventoId(), boleta.categoria(), boleta.getPrecioPagado(),
                boleta.getFechaCompra().truncatedTo(ChronoUnit.SECONDS), codigoQr,
                new Comprador(compra.compradorId(), compra.nombreComprador(), compra.correoComprador()),
                new Evento(evento.nombre(), evento.fechaInicio(), evento.lugar()));
        return new BoletaCompradaMensaje(UUID.randomUUID(), TIPO, 1,
                Instant.now().truncatedTo(ChronoUnit.SECONDS), "boleteria", datos);
    }
}