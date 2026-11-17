package com.eventhub.boleteria.domain;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class FabricaBoletaVIP extends FabricaBoleta {

    static final List<String> BENEFICIOS = List.of("Ingreso preferencial", "Zona VIP");

    @Override
    public TipoBoleta tipo() { return TipoBoleta.VIP; }

    @Override
    protected Boleta crearBoleta(Compra compra, long precio) {
        return new BoletaVIP(UUID.randomUUID(), compra.eventoId(), compra.compradorId(), precio,
                EstadoBoleta.ACTIVA, Instant.now(), BENEFICIOS);
    }
}