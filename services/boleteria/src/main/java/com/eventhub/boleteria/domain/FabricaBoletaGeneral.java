package com.eventhub.boleteria.domain;

import java.time.Instant;
import java.util.UUID;

public class FabricaBoletaGeneral extends FabricaBoleta {

    @Override
    public TipoBoleta tipo() { return TipoBoleta.GENERAL; }

    @Override
    protected Boleta crearBoleta(Compra compra, long precio) {
        return new BoletaGeneral(UUID.randomUUID(), compra.eventoId(), compra.compradorId(), precio,
                EstadoBoleta.ACTIVA, Instant.now());
    }
}