package com.eventhub.boleteria.infrastructure.pagos;

import com.eventhub.boleteria.application.PasarelaPagos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Pasarela de pagos simulada (en el C4 es un sistema externo). Aprueba todos los cobros.
 * Para usar una real, se reemplaza esta clase por otro adaptador del mismo puerto.
 */
@Component
public class PasarelaPagosSimulada implements PasarelaPagos {

    private static final Logger log = LoggerFactory.getLogger(PasarelaPagosSimulada.class);

    @Override
    public ResultadoPago cobrar(UUID compradorId, long monto, String concepto) {
        var referencia = "SIM-" + UUID.randomUUID();
        log.info("Pago simulado aprobado: {} COP, {} (comprador {}, ref {})", monto, concepto, compradorId, referencia);
        return ResultadoPago.aprobado(referencia);
    }
}