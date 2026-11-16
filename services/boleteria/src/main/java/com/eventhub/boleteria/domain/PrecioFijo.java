package com.eventhub.boleteria.domain;

public class PrecioFijo implements PrecioStrategy {

    @Override
    public TipoPrecio tipo() { return TipoPrecio.FIJO; }

    @Override
    public long calcular(ContextoPrecio contexto) {
        return contexto.precioBase();
    }
}