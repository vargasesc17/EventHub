package com.eventhub.boleteria.domain;

import java.time.Instant;

/** Parámetros de la estrategia de una categoría. Cada estrategia usa solo los suyos; el resto va en null. */
public record ParametrosPrecio(Instant fechaLimite, Integer descuentoPorcentaje,
                               Integer incrementoMaximoPorcentaje) {

    public static ParametrosPrecio ninguno() {
        return new ParametrosPrecio(null, null, null);
    }

    public static ParametrosPrecio earlyBird(Instant fechaLimite, int descuentoPorcentaje) {
        return new ParametrosPrecio(fechaLimite, descuentoPorcentaje, null);
    }

    public static ParametrosPrecio dinamico(int incrementoMaximoPorcentaje) {
        return new ParametrosPrecio(null, null, incrementoMaximoPorcentaje);
    }
}