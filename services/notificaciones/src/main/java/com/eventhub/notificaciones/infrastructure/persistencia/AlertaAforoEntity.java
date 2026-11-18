package com.eventhub.notificaciones.infrastructure.persistencia;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "alertas_aforo")
public class AlertaAforoEntity {

    @Id
    private UUID eventoId;
    private Instant enviadaEn;

    protected AlertaAforoEntity() { }   // lo exige JPA

    public AlertaAforoEntity(UUID eventoId, Instant enviadaEn) {
        this.eventoId = eventoId;
        this.enviadaEn = enviadaEn;
    }

    public UUID getEventoId() { return eventoId; }
    public Instant getEnviadaEn() { return enviadaEn; }
}
