package com.eventhub.eventos.infrastructure;

import com.eventhub.eventos.domain.EstadoEvento;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "eventos")
public class EventoEntity {

    @Id
    private UUID id;
    private String nombre;
    private String lugar;
    private Instant fechaInicio;
    private int aforoMaximo;
    private UUID organizadorId;
    private String organizadorCorreo;

    @Enumerated(EnumType.STRING)
    private EstadoEvento estado;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "categorias_boleta", joinColumns = @JoinColumn(name = "evento_id"))
    private List<CategoriaBoletaEmbeddable> categorias = new ArrayList<>();

    protected EventoEntity() { }   // lo exige JPA

    public EventoEntity(UUID id, String nombre, String lugar, Instant fechaInicio, int aforoMaximo,
                        UUID organizadorId, String organizadorCorreo, EstadoEvento estado,
                        List<CategoriaBoletaEmbeddable> categorias) {
        this.id = id;
        this.nombre = nombre;
        this.lugar = lugar;
        this.fechaInicio = fechaInicio;
        this.aforoMaximo = aforoMaximo;
        this.organizadorId = organizadorId;
        this.organizadorCorreo = organizadorCorreo;
        this.estado = estado;
        this.categorias = new ArrayList<>(categorias);
    }

    public UUID getId() { return id; }
    public String getNombre() { return nombre; }
    public String getLugar() { return lugar; }
    public Instant getFechaInicio() { return fechaInicio; }
    public int getAforoMaximo() { return aforoMaximo; }
    public UUID getOrganizadorId() { return organizadorId; }
    public String getOrganizadorCorreo() { return organizadorCorreo; }
    public EstadoEvento getEstado() { return estado; }
    public List<CategoriaBoletaEmbeddable> getCategorias() { return categorias; }
}