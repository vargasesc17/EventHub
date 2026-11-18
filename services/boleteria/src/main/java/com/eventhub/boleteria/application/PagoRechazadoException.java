package com.eventhub.boleteria.application;

/** La pasarela no aprobó el cobro. Se responde 402. */
public class PagoRechazadoException extends RuntimeException {
    public PagoRechazadoException(String motivo) {
        super("El pago fue rechazado: " + motivo);
    }
}