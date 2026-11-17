package com.eventhub.notificaciones.infrastructure.persistencia;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "compradores_evento")
public class CompradorEventoEntity {

    @Id
    private UUID boletaId;
    private UUID eventoId;
    private UUID compradorId;
    private String nombre;
    private String correo;
    private Instant registradoEn;

    protected CompradorEventoEntity() { }   // lo exige JPA

    public CompradorEventoEntity(UUID boletaId, UUID eventoId, UUID compradorId, String nombre,
                                 String correo, Instant registradoEn) {
        this.boletaId = boletaId;
        this.eventoId = eventoId;
        this.compradorId = compradorId;
        this.nombre = nombre;
        this.correo = correo;
        this.registradoEn = registradoEn;
    }

    public UUID getBoletaId() { return boletaId; }
    public UUID getEventoId() { return eventoId; }
    public UUID getCompradorId() { return compradorId; }
    public String getNombre() { return nombre; }
    public String getCorreo() { return correo; }
    public Instant getRegistradoEn() { return registradoEn; }
}
