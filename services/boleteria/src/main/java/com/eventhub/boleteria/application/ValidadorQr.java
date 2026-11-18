package com.eventhub.boleteria.application;

import java.util.Optional;
import java.util.UUID;

/**
 * Primer paso del check-in (ADR-0004): revisa la firma del QR SIN consultar la base de datos.
 * Si la firma es correcta, devuelve el id de la boleta; si el QR fue alterado o inventado, vacío.
 */
public interface ValidadorQr {
    Optional<UUID> boletaIdSiEsAutentico(String codigoQr);
}
