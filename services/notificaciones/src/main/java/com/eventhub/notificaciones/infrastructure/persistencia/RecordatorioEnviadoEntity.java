package com.eventhub.notificaciones.infrastructure.persistencia;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Un recordatorio ya enviado. Se identifica por boleta + fecha del evento: si el evento cambia de
 * fecha, la fecha nueva es otro id y por eso se envía un recordatorio nuevo.
 */
@Entity
@Table(name = "recordatorios_enviados")
public class RecordatorioEnviadoEntity {

    @Id
    private String id;
    private UUID boletaId;
    private Instant fechaInicioEvento;
    private Instant enviadoEn;

    protected RecordatorioEnviadoEntity() { }   // lo exige JPA

    public RecordatorioEnviadoEntity(UUID boletaId, Instant fechaInicioEvento, Instant enviadoEn) {
        this.id = idDe(boletaId, fechaInicioEvento);
        this.boletaId = boletaId;
        this.fechaInicioEvento = fechaInicioEvento;
        this.enviadoEn = enviadoEn;
    }

    static String idDe(UUID boletaId, Instant fechaInicioEvento) {
        return boletaId + "@" + fechaInicioEvento.toEpochMilli();
    }

    public String getId() { return id; }
    public UUID getBoletaId() { return boletaId; }
    public Instant getFechaInicioEvento() { return fechaInicioEvento; }
    public Instant getEnviadoEn() { return enviadoEn; }
}
