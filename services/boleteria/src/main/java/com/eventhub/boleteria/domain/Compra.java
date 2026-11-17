package com.eventhub.boleteria.domain;

import java.util.UUID;

/** Lo que llega al comprar. carnetEstudiantil y emitidaPor solo aplican a Estudiante y Cortesía. */
public record Compra(UUID eventoId, TipoBoleta categoria, UUID compradorId, String nombreComprador,
                     String correoComprador, String carnetEstudiantil, UUID emitidaPor) {

    public Compra {
        if (eventoId == null || categoria == null || compradorId == null) {
            throw new ReglaDeNegocioException("La compra exige evento, categoría y comprador");
        }
    }
}