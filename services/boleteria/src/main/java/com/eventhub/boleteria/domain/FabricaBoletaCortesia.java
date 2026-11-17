package com.eventhub.boleteria.domain;

import java.time.Instant;
import java.util.UUID;

public class FabricaBoletaCortesia extends FabricaBoleta {

    @Override
    public TipoBoleta tipo() { return TipoBoleta.CORTESIA; }

    @Override
    protected Boleta crearBoleta(Compra compra, long precio) {
        return new BoletaCortesia(UUID.randomUUID(), compra.eventoId(), compra.compradorId(), precio,
                EstadoBoleta.ACTIVA, Instant.now(), compra.emitidaPor());
    }
}