package com.eventhub.eventos.domain;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class Evento {

    private final UUID id;
    private String nombre;
    private String lugar;
    private Instant fechaInicio;
    private int aforoMaximo;
    private final UUID organizadorId;
    private final String organizadorCorreo;
    private EstadoEvento estado;
    private List<CategoriaBoleta> categorias;

    // Privado: nadie hace "new Evento(...)" desde afuera; se usan las fábricas de abajo
    private Evento(UUID id, String nombre, String lugar, Instant fechaInicio, int aforoMaximo,
                   UUID organizadorId, String organizadorCorreo, EstadoEvento estado,
                   List<CategoriaBoleta> categorias) {
        validar(nombre, lugar, fechaInicio, aforoMaximo, categorias);
        this.id = id;
        this.nombre = nombre;
        this.lugar = lugar;
        this.fechaInicio = fechaInicio;
        this.aforoMaximo = aforoMaximo;
        this.organizadorId = organizadorId;
        this.organizadorCorreo = organizadorCorreo;
        this.estado = estado;
        this.categorias = List.copyOf(categorias);
    }

    /** Un evento NUEVO: genera su id, siempre empieza en BORRADOR y no puede programarse en el pasado. */
    public static Evento crear(String nombre, String lugar, Instant fechaInicio, int aforoMaximo,
                               UUID organizadorId, String organizadorCorreo,
                               List<CategoriaBoleta> categorias, Instant ahora) {
        exigirFechaFutura(fechaInicio, ahora);
        return new Evento(UUID.randomUUID(), nombre, lugar, fechaInicio, aforoMaximo,
                organizadorId, organizadorCorreo, EstadoEvento.BORRADOR, categorias);
    }
    /** Un evento que YA EXISTE (viene de la base): conserva su id y su estado. */
    public static Evento reconstruir(UUID id, String nombre, String lugar, Instant fechaInicio, int aforoMaximo,
                                     UUID organizadorId, String organizadorCorreo, EstadoEvento estado,
                                     List<CategoriaBoleta> categorias) {
        return new Evento(id, nombre, lugar, fechaInicio, aforoMaximo, organizadorId, organizadorCorreo, estado, categorias);
    }

    /** Solo se puede editar en BORRADOR (decisión del #19). */
    public void editar(String nombre, String lugar, Instant fechaInicio, int aforoMaximo,
                       List<CategoriaBoleta> categorias, Instant ahora) {
        if (estado != EstadoEvento.BORRADOR) {
            throw new ReglaDeNegocioException("Solo se puede editar un evento en estado BORRADOR (estado: " + estado + ")");
        }
        exigirFechaFutura(fechaInicio, ahora);
        validar(nombre, lugar, fechaInicio, aforoMaximo, categorias);

        this.nombre = nombre;
        this.lugar = lugar;
        this.fechaInicio = fechaInicio;
        this.aforoMaximo = aforoMaximo;
        this.categorias = List.copyOf(categorias);
    }

    public void publicar()  { cambiarEstado(EstadoEvento.PUBLICADO); }
    public void iniciar()   { cambiarEstado(EstadoEvento.EN_CURSO); }
    public void finalizar() { cambiarEstado(EstadoEvento.FINALIZADO); }
    public void cancelar()  { cambiarEstado(EstadoEvento.CANCELADO); }

    /** Cambia la fecha de un evento ya publicado. Hay compradores que avisar (RF-04). */
    public void cambiarFecha(Instant nueva, Instant ahora) {
        exigirPublicado("cambiar la fecha");
        if (nueva == null) {
            throw new ReglaDeNegocioException("La fecha de inicio del evento es obligatoria");
        }
        exigirFechaFutura(nueva, ahora);
        this.fechaInicio = nueva;
    }

    /** Cambia el lugar de un evento ya publicado. */
    public void cambiarLugar(String nuevo) {
        exigirPublicado("cambiar el lugar");
        if (nuevo == null || nuevo.isBlank()) {
            throw new ReglaDeNegocioException("El lugar del evento es obligatorio");
        }
        this.lugar = nuevo;
    }

    private void cambiarEstado(EstadoEvento destino) {
        if (!estado.puedePasarA(destino)) {
            throw new ReglaDeNegocioException(
                    "Un evento en estado " + estado + " no puede pasar a " + destino);
        }
        this.estado = destino;
    }

    private void exigirPublicado(String accion) {
        if (estado != EstadoEvento.PUBLICADO) {
            throw new ReglaDeNegocioException(
                    "Solo se puede " + accion + " de un evento PUBLICADO (estado: " + estado + ")");
        }
    }

    /** Programar o reprogramar exige una fecha futura. Si es null, lo reporta validar con su mensaje. */
    private static void exigirFechaFutura(Instant fechaInicio, Instant ahora) {
        if (ahora == null) {
            throw new IllegalArgumentException("Se necesita la hora actual para validar la fecha");
        }
        if (fechaInicio != null && !fechaInicio.isAfter(ahora)) {
            throw new ReglaDeNegocioException("La fecha de inicio debe ser futura (recibida: " + fechaInicio
                    + ", ahora: " + ahora + ")");
        }
    }

    private static void validar(String nombre, String lugar, Instant fechaInicio, int aforoMaximo,
                                List<CategoriaBoleta> categorias) {

        if (nombre == null || nombre.isBlank()) {
            throw new ReglaDeNegocioException("El nombre del evento es obligatorio (nombre: " + nombre + ")");
        }
        if (lugar == null || lugar.isBlank()) {
            throw new ReglaDeNegocioException("El lugar del evento es obligatorio (lugar: " + lugar + ")");
        }
        if (fechaInicio == null) {
            throw new ReglaDeNegocioException("La fecha de inicio del evento es obligatoria (fechaInicio: " + fechaInicio + ")");
        }
        if (aforoMaximo <= 0) {
            throw new ReglaDeNegocioException("El aforo máximo del evento debe ser positivo (aforoMaximo: " + aforoMaximo + ")");
        }
        if (categorias == null || categorias.isEmpty()) {
            throw new ReglaDeNegocioException("Las categorías del evento son obligatorias (categorias: " + categorias + ")");
        }

        int sumaCupos = categorias.stream().mapToInt(CategoriaBoleta::cupos).sum();
        if (sumaCupos > aforoMaximo) {
            throw new ReglaDeNegocioException("La suma de cupos (" + sumaCupos
                    + ") supera el aforo máximo (" + aforoMaximo + ")");
        }
    }

    public UUID getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getLugar() {
        return lugar;
    }

    public Instant getFechaInicio() {
        return fechaInicio;
    }

    public int getAforoMaximo() {
        return aforoMaximo;
    }

    public UUID getOrganizadorId() {
        return organizadorId;
    }

    public String getOrganizadorCorreo() {
        return organizadorCorreo;
    }

    public EstadoEvento getEstado() {
        return estado;
    }

    public List<CategoriaBoleta> getCategorias() {
        return categorias;
    }
}