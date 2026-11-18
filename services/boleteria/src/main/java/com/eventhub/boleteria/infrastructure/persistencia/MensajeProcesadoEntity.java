package com.eventhub.boleteria.infrastructure.persistencia;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "mensajes_procesados")
public class MensajeProcesadoEntity {

    @Id
    private UUID messageId;
    private String tipo;
    private Instant procesadoEn;

    protected MensajeProcesadoEntity() { }   // lo exige JPA

    public MensajeProcesadoEntity(UUID messageId, String tipo, Instant procesadoEn) {
        this.messageId = messageId;
        this.tipo = tipo;
        this.procesadoEn = procesadoEn;
    }

    public UUID getMessageId() { return messageId; }
    public String getTipo() { return tipo; }
    public Instant getProcesadoEn() { return procesadoEn; }
}