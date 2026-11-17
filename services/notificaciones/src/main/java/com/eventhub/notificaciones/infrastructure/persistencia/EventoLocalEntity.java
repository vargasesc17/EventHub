package com.eventhub.notificaciones.infrastructure.persistencia;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "eventos_local")
public class EventoLocalEntity {

    @Id
    private UUID eventoId;
    private String nombre;
    private Instant fechaInicio;
    private String lugar;
    private boolean cancelado;
    private Instant ultimaFechaEmision;

    protected EventoLocalEntity() { }   // lo exige JPA

    public EventoLocalEntity(UUID eventoId, String nombre, Instant fechaInicio, String lugar,
                             boolean cancelado, Instant ultimaFechaEmision) {
        this.eventoId = eventoId;
        this.nombre = nombre;
        this.fechaInicio = fechaInicio;
        this.lugar = lugar;
        this.cancelado = cancelado;
        this.ultimaFechaEmision = ultimaFechaEmision;
    }

    public UUID getEventoId() { return eventoId; }
    public String getNombre() { return nombre; }
    public Instant getFechaInicio() { return fechaInicio; }
    public String getLugar() { return lugar; }
    public boolean isCancelado() { return cancelado; }
    public Instant getUltimaFechaEmision() { return ultimaFechaEmision; }
}
