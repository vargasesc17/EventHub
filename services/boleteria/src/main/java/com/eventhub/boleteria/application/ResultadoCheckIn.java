package com.eventhub.boleteria.application;

import com.eventhub.boleteria.domain.TipoBoleta;

import java.util.UUID;

/** Respuesta del check-in para el staff: si entra o no, y por qué. */
public record ResultadoCheckIn(boolean aceptado, String motivo, UUID boletaId, TipoBoleta categoria) {

    public static ResultadoCheckIn aceptado(UUID boletaId, TipoBoleta categoria) {
        return new ResultadoCheckIn(true, null, boletaId, categoria);
    }

    public static ResultadoCheckIn rechazado(String motivo, UUID boletaId) {
        return new ResultadoCheckIn(false, motivo, boletaId, null);
    }
}
