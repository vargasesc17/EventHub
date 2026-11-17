package com.eventhub.boleteria.domain;

import java.time.Instant;
import java.util.UUID;

public class BoletaCortesia extends Boleta {

    private final UUID emitidaPor;

    public BoletaCortesia(UUID id, UUID eventoId, UUID compradorId, long precioPagado,
                          EstadoBoleta estado, Instant fechaCompra, UUID emitidaPor) {
        super(id, eventoId, compradorId, precioPagado, estado, fechaCompra);
        if (emitidaPor == null) {
            throw new ReglaDeNegocioException("Una boleta de cortesía exige quién la emitió");
        }
        if (precioPagado != 0) {
            throw new ReglaDeNegocioException("Una boleta de cortesía no se cobra (recibido: " + precioPagado + ")");
        }
        this.emitidaPor = emitidaPor;
    }

    @Override
    public TipoBoleta categoria() { return TipoBoleta.CORTESIA; }

    public UUID getEmitidaPor() { return emitidaPor; }
}