package com.eventhub.boleteria.infrastructure.persistencia;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "check_in")
public class CheckInEntity {

    @Id
    private UUID id;
    private UUID boletaId;
    private UUID eventoId;
    private UUID staffId;
    private Instant fecha;

    protected CheckInEntity() { }   // lo exige JPA

    public CheckInEntity(UUID id, UUID boletaId, UUID eventoId, UUID staffId, Instant fecha) {
        this.id = id;
        this.boletaId = boletaId;
        this.eventoId = eventoId;
        this.staffId = staffId;
        this.fecha = fecha;
    }

    public UUID getId() { return id; }
    public UUID getBoletaId() { return boletaId; }
    public UUID getEventoId() { return eventoId; }
    public UUID getStaffId() { return staffId; }
    public Instant getFecha() { return fecha; }
}
