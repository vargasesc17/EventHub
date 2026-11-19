package com.eventhub.boleteria.interfaces;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/** Lo que manda la app del staff al escanear: el texto del QR, el evento de esa entrada y quién valida. */
public record CheckInRequest(@NotBlank String codigoQr, @NotNull UUID eventoId, @NotNull UUID staffId) { }
