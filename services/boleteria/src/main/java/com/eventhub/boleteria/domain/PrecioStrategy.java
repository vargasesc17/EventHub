package com.eventhub.boleteria.domain;

public interface PrecioStrategy {

    /** Qué tipo de precio implementa esta estrategia. */
    TipoPrecio tipo();

    /** Precio de UNA boleta en pesos, sin decimales. */
    long calcular(ContextoPrecio contexto);
}