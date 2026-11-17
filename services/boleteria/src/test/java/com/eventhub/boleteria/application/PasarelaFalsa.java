package com.eventhub.boleteria.application;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

class PasarelaFalsa implements PasarelaPagos {
    private final List<Long> cobros = new ArrayList<>();
    private boolean aprueba = true;

    void rechazar() { aprueba = false; }

    List<Long> cobros() { return cobros; }

    @Override
    public ResultadoPago cobrar(UUID compradorId, long monto, String concepto) {
        if (!aprueba) {
            return ResultadoPago.rechazado("Fondos insuficientes");
        }
        cobros.add(monto);
        return ResultadoPago.aprobado("PAGO-" + cobros.size());
    }
}