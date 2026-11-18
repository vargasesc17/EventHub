package com.eventhub.boleteria.interfaces.mensajes;

/**
 * El mensaje no cumple el contrato (JSON mal formado o falta un campo obligatorio).
 * Reintentarlo no lo va a arreglar, así que va directo a la DLQ, sin reintentos (contrato #15).
 */
public class MensajeInvalidoException extends RuntimeException {

    public MensajeInvalidoException(String mensaje) {
        super(mensaje);
    }

    public MensajeInvalidoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
