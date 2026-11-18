package com.eventhub.boleteria.domain;

import java.time.Instant;
import java.util.UUID;

public abstract class Boleta {

    private final UUID id;
    private final UUID eventoId;
    private final UUID compradorId;
    private final long precioPagado;
    private EstadoBoleta estado;
    private final Instant fechaCompra;

    protected Boleta(UUID id, UUID eventoId, UUID compradorId, long precioPagado,
                     EstadoBoleta estado, Instant fechaCompra) {
        if (id == null || eventoId == null || compradorId == null || estado == null || fechaCompra == null) {
            throw new ReglaDeNegocioException("A la boleta le faltan datos obligatorios");
        }
        if (precioPagado < 0) {
            throw new ReglaDeNegocioException("El precio pagado no puede ser negativo (recibido: " + precioPagado + ")");
        }
        this.id = id;
        this.eventoId = eventoId;
        this.compradorId = compradorId;
        this.precioPagado = precioPagado;
        this.estado = estado;
        this.fechaCompra = fechaCompra;
    }

    /**
     * ¿Esta boleta puede entrar al evento? Solo si está ACTIVA (pagada y sin usar).
     * Es final a propósito: la regla es la MISMA para VIP, General, Estudiante y Cortesía, y
     * ninguna subclase la puede cambiar. Así el check-in trata a cualquier Boleta igual (LSP, #24).
     */
    public final boolean puedeIngresar() {
        return estado == EstadoBoleta.ACTIVA;
    }

    /** Cada subclase dice qué tipo es. */
    public abstract TipoBoleta categoria();

    public UUID getId() { return id; }
    public UUID getEventoId() { return eventoId; }
    public UUID getCompradorId() { return compradorId; }
    public long getPrecioPagado() { return precioPagado; }
    public EstadoBoleta getEstado() { return estado; }
    public Instant getFechaCompra() { return fechaCompra; }
}