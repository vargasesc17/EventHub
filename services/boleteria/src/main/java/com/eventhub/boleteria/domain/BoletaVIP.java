package com.eventhub.boleteria.domain;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class BoletaVIP extends Boleta {

    private final List<String> beneficios;

    public BoletaVIP(UUID id, UUID eventoId, UUID compradorId, long precioPagado,
                     EstadoBoleta estado, Instant fechaCompra, List<String> beneficios) {
        super(id, eventoId, compradorId, precioPagado, estado, fechaCompra);
        if (beneficios == null || beneficios.isEmpty()) {
            throw new ReglaDeNegocioException("Una boleta VIP debe tener al menos un beneficio");
        }
        this.beneficios = List.copyOf(beneficios);
    }

    @Override
    public TipoBoleta categoria() { return TipoBoleta.VIP; }

    public List<String> getBeneficios() { return beneficios; }
}