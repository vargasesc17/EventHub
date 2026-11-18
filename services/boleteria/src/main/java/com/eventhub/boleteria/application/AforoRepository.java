package com.eventhub.boleteria.application;

import com.eventhub.boleteria.domain.AforoEvento;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/** Copia local de los eventos. Cada cambio toca solo sus columnas, para no pisar los contadores de venta. */
public interface AforoRepository {

    boolean existe(UUID eventoId);

    void crear(AforoEvento aforo);

    Optional<AforoEvento> buscar(UUID eventoId);

    void actualizarProgramacion(UUID eventoId, Instant fechaInicio, String lugar);

    void bloquearVentas(UUID eventoId);

    /** Suma una venta al evento. */
    void sumarVenta(UUID eventoId);

    /**
     * Marca la alerta como enviada SOLO si todavía no lo estaba y las vendidas alcanzan el umbral
     * (ADR-0003). Devuelve true únicamente a la compra que logra marcarla.
     */
    boolean marcarAlertaSiAlcanzaUmbral(UUID eventoId, int umbralPorcentaje);
}