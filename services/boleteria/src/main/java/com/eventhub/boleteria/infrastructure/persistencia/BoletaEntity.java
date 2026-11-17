package com.eventhub.boleteria.infrastructure.persistencia;

import com.eventhub.boleteria.domain.EstadoBoleta;
import com.eventhub.boleteria.domain.TipoBoleta;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Las cuatro clases de boleta en UNA tabla (single table). Los campos propios de cada tipo
 * quedan en null para los demás. La tabla se llama "boleta", como en el SQL del ADR-0004.
 */
@Entity
@Table(name = "boleta")
public class BoletaEntity {

    @Id
    private UUID id;
    private UUID eventoId;
    private UUID compradorId;
    @Enumerated(EnumType.STRING)
    private TipoBoleta categoria;
    private long precioPagado;
    @Enumerated(EnumType.STRING)
    private EstadoBoleta estado;
    private Instant fechaCompra;
    private String beneficios;          // solo VIP, separados por "|"
    private String carnetEstudiantil;   // solo Estudiante
    private UUID emitidaPor;            // solo Cortesía

    protected BoletaEntity() { }   // lo exige JPA

    public BoletaEntity(UUID id, UUID eventoId, UUID compradorId, TipoBoleta categoria, long precioPagado,
                        EstadoBoleta estado, Instant fechaCompra, String beneficios, String carnetEstudiantil,
                        UUID emitidaPor) {
        this.id = id;
        this.eventoId = eventoId;
        this.compradorId = compradorId;
        this.categoria = categoria;
        this.precioPagado = precioPagado;
        this.estado = estado;
        this.fechaCompra = fechaCompra;
        this.beneficios = beneficios;
        this.carnetEstudiantil = carnetEstudiantil;
        this.emitidaPor = emitidaPor;
    }

    public UUID getId() { return id; }
    public UUID getEventoId() { return eventoId; }
    public UUID getCompradorId() { return compradorId; }
    public TipoBoleta getCategoria() { return categoria; }
    public long getPrecioPagado() { return precioPagado; }
    public EstadoBoleta getEstado() { return estado; }
    public Instant getFechaCompra() { return fechaCompra; }
    public String getBeneficios() { return beneficios; }
    public String getCarnetEstudiantil() { return carnetEstudiantil; }
    public UUID getEmitidaPor() { return emitidaPor; }
}