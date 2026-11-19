package com.eventhub.eventos.application;

import com.eventhub.eventos.domain.EstadoEvento;
import com.eventhub.eventos.domain.Evento;
import com.eventhub.eventos.domain.TipoCambio;
import com.eventhub.eventos.domain.ValoresAnteriores;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;
import java.time.Clock;


public class GestionarEventoUseCase {

    private final EventoRepository repositorio;
    private final PublicadorEventos publicador;
    private final Clock reloj;

    public GestionarEventoUseCase(EventoRepository repositorio, PublicadorEventos publicador, Clock reloj) {
        this.repositorio = repositorio;
        this.publicador = publicador;
        this.reloj = reloj;
    }

    public Evento crear(CrearEventoCommand datos) {
        var evento = Evento.crear(datos.nombre(), datos.lugar(), datos.fechaInicio(), datos.aforoMaximo(),
                datos.organizadorId(), datos.organizadorCorreo(), datos.categorias(), reloj.instant());
        repositorio.guardar(evento);
        return evento;
    }

    public Evento editar(UUID id, EditarEventoCommand datos) {
        var evento = cargar(id);
        evento.editar(datos.nombre(), datos.lugar(), datos.fechaInicio(), datos.aforoMaximo(),
                datos.categorias(), reloj.instant());
        repositorio.guardar(evento);
        return evento;
    }

    public Evento publicar(UUID id) {
        var evento = cargar(id);
        evento.publicar();
        repositorio.guardar(evento);
        publicador.eventoActualizado(evento, Set.of(TipoCambio.PUBLICADO), null);
        return evento;
    }

    public Evento iniciar(UUID id) {
        var evento = cargar(id);
        evento.iniciar();
        repositorio.guardar(evento);
        return evento;
    }

    public Evento finalizar(UUID id) {
        var evento = cargar(id);
        evento.finalizar();
        repositorio.guardar(evento);
        return evento;
    }

    public Evento cancelar(UUID id) {
        var evento = cargar(id);
        boolean estabaPublicado = evento.getEstado() == EstadoEvento.PUBLICADO;
        evento.cancelar();
        repositorio.guardar(evento);
        // Un borrador cancelado nunca se anunció: nadie lo conoce, no hay a quién avisar.
        if (estabaPublicado) {
            publicador.eventoActualizado(evento, Set.of(TipoCambio.CANCELADO), null);
        }
        return evento;
    }

    /** Cambia fecha y/o lugar de un evento publicado. Publica UN solo mensaje con todos los cambios. */
    public Evento reprogramar(UUID id, ReprogramarEventoCommand datos) {
        var evento = cargar(id);
        var cambios = EnumSet.noneOf(TipoCambio.class);
        Instant fechaAnterior = null;
        String lugarAnterior = null;

        if (datos.fechaInicio() != null && !datos.fechaInicio().equals(evento.getFechaInicio())) {
            fechaAnterior = evento.getFechaInicio();
            evento.cambiarFecha(datos.fechaInicio(), reloj.instant());
            cambios.add(TipoCambio.FECHA);
        }
        if (datos.lugar() != null && !datos.lugar().equals(evento.getLugar())) {
            lugarAnterior = evento.getLugar();
            evento.cambiarLugar(datos.lugar());
            cambios.add(TipoCambio.LUGAR);
        }
        if (cambios.isEmpty()) {
            return evento;   // nada cambió: ni se guarda ni se avisa
        }

        repositorio.guardar(evento);
        publicador.eventoActualizado(evento, cambios, new ValoresAnteriores(fechaAnterior, lugarAnterior));
        return evento;
    }

    private Evento cargar(UUID id) {
        return repositorio.buscarPorId(id).orElseThrow(() -> new EventoNoEncontradoException(id));
    }
}