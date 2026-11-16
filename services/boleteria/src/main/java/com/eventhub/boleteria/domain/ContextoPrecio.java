package com.eventhub.boleteria.domain;

import java.time.Instant;

/** Lo único que una estrategia necesita saber para calcular el precio de una boleta. */
public record ContextoPrecio(long precioBase, int cupos, int vendidas, Instant fechaCompra,
                             ParametrosPrecio parametros) {

    public ContextoPrecio {
        if (precioBase < 0) {
            throw new ReglaDeNegocioException("El precio base no puede ser negativo (recibido: " + precioBase + ")");
        }
        if (cupos <= 0) {
            throw new ReglaDeNegocioException("Los cupos deben ser mayores que 0 (recibido: " + cupos + ")");
        }
        if (vendidas < 0 || vendidas > cupos) {
            throw new ReglaDeNegocioException("Las vendidas deben estar entre 0 y " + cupos + " (recibido: " + vendidas + ")");
        }
        if (fechaCompra == null || parametros == null) {
            throw new ReglaDeNegocioException("El cálculo del precio exige fecha de compra y parámetros");
        }
    }
}