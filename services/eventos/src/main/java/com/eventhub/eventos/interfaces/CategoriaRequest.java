package com.eventhub.eventos.interfaces;

import com.eventhub.eventos.domain.CategoriaBoleta;
import com.eventhub.eventos.domain.TipoBoleta;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record CategoriaRequest(@NotNull TipoBoleta tipo,
                               @Positive int cupos,
                               @PositiveOrZero long precioBase) {

    CategoriaBoleta aDominio() {
        return new CategoriaBoleta(tipo, cupos, precioBase);
    }
}