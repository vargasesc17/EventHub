package com.eventhub.boleteria.domain;

import java.time.Instant;
import java.util.UUID;

public class BoletaGeneral extends Boleta {

    public BoletaGeneral(UUID id, UUID eventoId, UUID compradorId, long precioPagado,
                         EstadoBoleta estado, Instant fechaCompra) {
        super(id, eventoId, compradorId, precioPagado, estado, fechaCompra);
    }

    @Override
    public TipoBoleta categoria() { return TipoBoleta.GENERAL; }
}