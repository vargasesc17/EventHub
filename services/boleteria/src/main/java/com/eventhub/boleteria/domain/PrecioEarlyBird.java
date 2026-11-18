package com.eventhub.boleteria.domain;

public class PrecioEarlyBird implements PrecioStrategy {

    @Override
    public TipoPrecio tipo() { return TipoPrecio.EARLY_BIRD; }

    @Override
    public long calcular(ContextoPrecio contexto) {
        var parametros = contexto.parametros();
        if (parametros.fechaLimite() == null || parametros.descuentoPorcentaje() == null) {
            throw new ReglaDeNegocioException("El precio early bird exige fecha límite y descuento");
        }
        int descuento = parametros.descuentoPorcentaje();
        if (descuento < 1 || descuento > 100) {
            throw new ReglaDeNegocioException("El descuento debe estar entre 1 y 100 (recibido: " + descuento + ")");
        }

        if (contexto.fechaCompra().isBefore(parametros.fechaLimite())) {
            return contexto.precioBase() * (100 - descuento) / 100;
        }
        return contexto.precioBase();
    }
}