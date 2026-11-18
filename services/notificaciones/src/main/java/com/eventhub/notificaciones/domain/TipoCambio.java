package com.eventhub.notificaciones.domain;

import java.util.Set;

/** Los cambios que puede traer un evento.actualizado (docs/contratos). */
public enum TipoCambio {
    PUBLICADO, FECHA, LUGAR, INVENTARIO, CANCELADO;

    /**
     * Los cambios que generan una alerta a los compradores (RF-04). PUBLICADO (todavía no hay
     * compradores) e INVENTARIO (no afecta a quien ya compró) se ignoran.
     */
    public static final Set<TipoCambio> ALERTAN_A_COMPRADORES = Set.of(FECHA, LUGAR, CANCELADO);
}
