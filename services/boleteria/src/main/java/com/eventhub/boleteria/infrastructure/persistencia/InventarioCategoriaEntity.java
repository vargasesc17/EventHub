package com.eventhub.boleteria.infrastructure.persistencia;

import com.eventhub.boleteria.domain.TipoBoleta;
import com.eventhub.boleteria.domain.TipoPrecio;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "inventario_categoria",
        uniqueConstraints = @UniqueConstraint(columnNames = {"evento_id", "categoria"}))
public class InventarioCategoriaEntity {

    @Id
    private UUID id;
    private UUID eventoId;
    @Enumerated(EnumType.STRING)
    private TipoBoleta categoria;
    private int cupos;
    private int disponibles;
    private int reservadas;
    private int vendidas;
    private long precioBase;

    protected InventarioCategoriaEntity() { }   // lo exige JPA

    public UUID getId() { return id; }
    public UUID getEventoId() { return eventoId; }
    public TipoBoleta getCategoria() { return categoria; }
    public int getCupos() { return cupos; }
    public int getDisponibles() { return disponibles; }
    public int getReservadas() { return reservadas; }
    public int getVendidas() { return vendidas; }
    public long getPrecioBase() { return precioBase; }

    // ... después de precioBase:
    @Enumerated(EnumType.STRING)
    private TipoPrecio tipoPrecio;
    private Instant fechaLimite;
    private Integer descuentoPorcentaje;
    private Integer incrementoMaximoPorcentaje;

    public InventarioCategoriaEntity(UUID id, UUID eventoId, TipoBoleta categoria, int cupos, int disponibles,
                                     int reservadas, int vendidas, long precioBase, TipoPrecio tipoPrecio,
                                     Instant fechaLimite, Integer descuentoPorcentaje,
                                     Integer incrementoMaximoPorcentaje) {
        this.id = id;
        this.eventoId = eventoId;
        this.categoria = categoria;
        this.cupos = cupos;
        this.disponibles = disponibles;
        this.reservadas = reservadas;
        this.vendidas = vendidas;
        this.precioBase = precioBase;
        this.tipoPrecio = tipoPrecio;
        this.fechaLimite = fechaLimite;
        this.descuentoPorcentaje = descuentoPorcentaje;
        this.incrementoMaximoPorcentaje = incrementoMaximoPorcentaje;
    }

    public TipoPrecio getTipoPrecio() { return tipoPrecio; }
    public Instant getFechaLimite() { return fechaLimite; }
    public Integer getDescuentoPorcentaje() { return descuentoPorcentaje; }
    public Integer getIncrementoMaximoPorcentaje() { return incrementoMaximoPorcentaje; }
}