package com.eventhub.eventos.domain;

public record CategoriaBoleta(TipoBoleta tipo, int cupos, long precioBase) {
    public CategoriaBoleta {
        if (tipo == null) {
            throw new ReglaDeNegocioException("El tipo de boleta es requerido");
        }
        if (cupos <= 0) {
            throw new ReglaDeNegocioException("Los cupos deben ser valor positivo; (recibido: " + cupos + ")");
        }
        if (precioBase < 0) {
            throw new ReglaDeNegocioException("Los precios deben ser valor positivo; (recibido: " + precioBase + ")");
        }
        if (tipo == TipoBoleta.CORTESIA && precioBase != 0) {
            throw new ReglaDeNegocioException("Una boleta de cortesía no tiene precio (recibido: " + precioBase + ")");
        }
    }

}
