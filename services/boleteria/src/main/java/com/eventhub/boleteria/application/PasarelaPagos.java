package com.eventhub.boleteria.application;

import java.util.UUID;

/** Cobra una compra. En esta entrega la implementación es simulada (C4: Pasarela de Pagos, sistema externo). */
public interface PasarelaPagos {

    ResultadoPago cobrar(UUID compradorId, long monto, String concepto);

    record ResultadoPago(boolean aprobado, String referencia, String motivoRechazo) {

        public static ResultadoPago aprobado(String referencia) {
            return new ResultadoPago(true, referencia, null);
        }

        public static ResultadoPago rechazado(String motivo) {
            return new ResultadoPago(false, null, motivo);
        }
    }
}