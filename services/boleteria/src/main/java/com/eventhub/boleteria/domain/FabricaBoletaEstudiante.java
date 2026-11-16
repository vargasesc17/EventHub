package com.eventhub.boleteria.domain;

import java.time.Instant;
import java.util.UUID;

public class FabricaBoletaEstudiante extends FabricaBoleta {

    @Override
    public TipoBoleta tipo() { return TipoBoleta.ESTUDIANTE; }

    @Override
    protected Boleta crearBoleta(Compra compra, long precio) {
        return new BoletaEstudiante(UUID.randomUUID(), compra.eventoId(), compra.compradorId(), precio,
                EstadoBoleta.ACTIVA, Instant.now(), compra.carnetEstudiantil());
    }
}