package com.eventhub.boleteria.application;

import java.util.UUID;

/** Genera el contenido del QR de una boleta (ADR-0004: boletaId + firma HMAC). */
public interface GeneradorQr {
    String generar(UUID boletaId);
}