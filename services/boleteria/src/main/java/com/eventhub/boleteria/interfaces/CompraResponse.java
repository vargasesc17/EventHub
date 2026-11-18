package com.eventhub.boleteria.interfaces;

import com.eventhub.boleteria.application.CompraRealizada;
import com.eventhub.boleteria.domain.EstadoBoleta;
import com.eventhub.boleteria.domain.TipoBoleta;

import java.time.Instant;
import java.util.UUID;

public record CompraResponse(UUID boletaId, UUID eventoId, TipoBoleta categoria, long precioPagado,
                             EstadoBoleta estado, Instant fechaCompra, String codigoQr) {

    static CompraResponse desde(CompraRealizada compra) {
        var b = compra.boleta();
        return new CompraResponse(b.getId(), b.getEventoId(), b.categoria(), b.getPrecioPagado(),
                b.getEstado(), b.getFechaCompra(), compra.codigoQr());
    }
}