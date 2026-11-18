package com.eventhub.boleteria.application;

import com.eventhub.boleteria.domain.TipoBoleta;

/** No quedan cupos en la categoría. Se responde 409: la solicitud era válida, pero llegó tarde. */
public class SinCuposException extends RuntimeException {
    public SinCuposException(TipoBoleta categoria) {
        super("No quedan boletas " + categoria + " para este evento");
    }
}