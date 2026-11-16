package com.eventhub.boleteria.domain;

import java.time.Instant;
import java.util.UUID;

public class BoletaEstudiante extends Boleta {

    private final String carnetEstudiantil;

    public BoletaEstudiante(UUID id, UUID eventoId, UUID compradorId, long precioPagado,
                            EstadoBoleta estado, Instant fechaCompra, String carnetEstudiantil) {
        super(id, eventoId, compradorId, precioPagado, estado, fechaCompra);
        if (carnetEstudiantil == null || carnetEstudiantil.isBlank()) {
            throw new ReglaDeNegocioException("Una boleta de estudiante exige el carnet estudiantil");
        }
        this.carnetEstudiantil = carnetEstudiantil;
    }

    @Override
    public TipoBoleta categoria() { return TipoBoleta.ESTUDIANTE; }

    public String getCarnetEstudiantil() { return carnetEstudiantil; }
}