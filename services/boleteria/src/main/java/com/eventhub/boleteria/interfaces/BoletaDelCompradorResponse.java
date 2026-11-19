package com.eventhub.boleteria.interfaces;

import com.eventhub.boleteria.application.ConsultarBoletasDelCompradorUseCase.BoletaDelComprador;
import com.eventhub.boleteria.domain.EstadoBoleta;
import com.eventhub.boleteria.domain.TipoBoleta;

import java.time.Instant;
import java.util.UUID;

/** Una boleta en "Mis boletas". nombreEvento, fechaEvento y lugar son null si Boletería no conoce el evento. */
public record BoletaDelCompradorResponse(UUID boletaId, UUID eventoId, String nombreEvento, Instant fechaEvento,
                                         String lugar, TipoBoleta categoria, long precioPagado, EstadoBoleta estado,
                                         Instant fechaCompra, String codigoQr) {

    static BoletaDelCompradorResponse desde(BoletaDelComprador item) {
        var b = item.boleta();
        var e = item.evento();
        return new BoletaDelCompradorResponse(b.getId(), b.getEventoId(),
                e == null ? null : e.nombre(), e == null ? null : e.fechaInicio(), e == null ? null : e.lugar(),
                b.categoria(), b.getPrecioPagado(), b.getEstado(), b.getFechaCompra(), item.codigoQr());
    }
}
