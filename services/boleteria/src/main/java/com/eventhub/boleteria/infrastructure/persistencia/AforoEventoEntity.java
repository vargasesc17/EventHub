package com.eventhub.boleteria.infrastructure.persistencia;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "aforo_evento")
public class AforoEventoEntity {

    @Id
    private UUID eventoId;
    private String nombre;
    private Instant fechaInicio;
    private String lugar;
    private UUID organizadorId;
    private String organizadorCorreo;
    private int aforoMaximo;
    private int vendidas;
    private boolean alertaEnviada;
    private boolean ventasBloqueadas;

    protected AforoEventoEntity() { }   // lo exige JPA

    public AforoEventoEntity(UUID eventoId, String nombre, Instant fechaInicio, String lugar, UUID organizadorId,
                             String organizadorCorreo, int aforoMaximo, int vendidas, boolean alertaEnviada,
                             boolean ventasBloqueadas) {
        this.eventoId = eventoId;
        this.nombre = nombre;
        this.fechaInicio = fechaInicio;
        this.lugar = lugar;
        this.organizadorId = organizadorId;
        this.organizadorCorreo = organizadorCorreo;
        this.aforoMaximo = aforoMaximo;
        this.vendidas = vendidas;
        this.alertaEnviada = alertaEnviada;
        this.ventasBloqueadas = ventasBloqueadas;
    }

    public UUID getEventoId() { return eventoId; }
    public String getNombre() { return nombre; }
    public Instant getFechaInicio() { return fechaInicio; }
    public String getLugar() { return lugar; }
    public UUID getOrganizadorId() { return organizadorId; }
    public String getOrganizadorCorreo() { return organizadorCorreo; }
    public int getAforoMaximo() { return aforoMaximo; }
    public int getVendidas() { return vendidas; }
    public boolean isAlertaEnviada() { return alertaEnviada; }
    public boolean isVentasBloqueadas() { return ventasBloqueadas; }
}