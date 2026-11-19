package com.eventhub.eventos.application;

import com.eventhub.eventos.domain.CategoriaBoleta;
import com.eventhub.eventos.domain.EstadoEvento;
import com.eventhub.eventos.domain.Evento;
import com.eventhub.eventos.domain.ReglaDeNegocioException;
import com.eventhub.eventos.domain.TipoBoleta;
import com.eventhub.eventos.domain.TipoCambio;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CicloDeVidaUseCaseTest {

    private final EventoRepositoryEnMemoria repositorio = new EventoRepositoryEnMemoria();
    private final PublicadorEventosEnMemoria publicador = new PublicadorEventosEnMemoria();
    private final GestionarEventoUseCase casoDeUso = new GestionarEventoUseCase(repositorio, publicador, Clock.systemUTC());
    private final Instant fechaOriginal = Instant.now().plus(30, ChronoUnit.DAYS);

    private UUID guardarEnEstado(EstadoEvento estado) {
        var evento = Evento.reconstruir(UUID.randomUUID(), "Concierto", "Movistar Arena", fechaOriginal, 1000,
                UUID.randomUUID(), "org@correo.com", estado,
                List.of(new CategoriaBoleta(TipoBoleta.GENERAL, 1000, 50_000)));
        repositorio.guardar(evento);
        return evento.getId();
    }

    private EstadoEvento estadoGuardado(UUID id) {
        return repositorio.buscarPorId(id).orElseThrow().getEstado();
    }

    @Test
    void publicarGuardaElEstadoYAvisaConPublicado() {
        var id = guardarEnEstado(EstadoEvento.BORRADOR);

        casoDeUso.publicar(id);

        assertEquals(EstadoEvento.PUBLICADO, estadoGuardado(id));
        assertEquals(1, publicador.publicaciones().size());
        var publicacion = publicador.publicaciones().get(0);
        assertEquals(Set.of(TipoCambio.PUBLICADO), publicacion.cambios());
        assertNull(publicacion.anteriores());
        assertEquals(id, publicacion.evento().getId());
    }

    @Test
    void cancelarUnPublicadoAvisaConCancelado() {
        var id = guardarEnEstado(EstadoEvento.PUBLICADO);

        casoDeUso.cancelar(id);

        assertEquals(EstadoEvento.CANCELADO, estadoGuardado(id));
        assertEquals(Set.of(TipoCambio.CANCELADO), publicador.publicaciones().get(0).cambios());
    }

    @Test
    void cancelarUnBorradorNoAvisaANadie() {
        var id = guardarEnEstado(EstadoEvento.BORRADOR);

        casoDeUso.cancelar(id);

        assertEquals(EstadoEvento.CANCELADO, estadoGuardado(id));
        assertTrue(publicador.publicaciones().isEmpty());
    }

    @Test
    void iniciarYFinalizarCambianElEstadoSinAvisar() {
        var id = guardarEnEstado(EstadoEvento.PUBLICADO);

        casoDeUso.iniciar(id);
        assertEquals(EstadoEvento.EN_CURSO, estadoGuardado(id));
        casoDeUso.finalizar(id);
        assertEquals(EstadoEvento.FINALIZADO, estadoGuardado(id));

        assertTrue(publicador.publicaciones().isEmpty());
    }

    @Test
    void reprogramarFechaYLugarAvisaEnUnSoloMensajeConLosValoresAnteriores() {
        var id = guardarEnEstado(EstadoEvento.PUBLICADO);
        var nuevaFecha = fechaOriginal.plus(2, ChronoUnit.DAYS);

        casoDeUso.reprogramar(id, new ReprogramarEventoCommand(nuevaFecha, "Coliseo, Chía"));

        assertEquals(1, publicador.publicaciones().size());
        var publicacion = publicador.publicaciones().get(0);
        assertEquals(Set.of(TipoCambio.FECHA, TipoCambio.LUGAR), publicacion.cambios());
        assertEquals(fechaOriginal, publicacion.anteriores().fechaInicio());
        assertEquals("Movistar Arena", publicacion.anteriores().lugar());
        assertEquals(nuevaFecha, publicacion.evento().getFechaInicio());
        assertEquals("Coliseo, Chía", publicacion.evento().getLugar());
    }

    @Test
    void reprogramarSoloElLugarNoMarcaLaFecha() {
        var id = guardarEnEstado(EstadoEvento.PUBLICADO);

        casoDeUso.reprogramar(id, new ReprogramarEventoCommand(null, "Coliseo, Chía"));

        var publicacion = publicador.publicaciones().get(0);
        assertEquals(Set.of(TipoCambio.LUGAR), publicacion.cambios());
        assertNull(publicacion.anteriores().fechaInicio());
        assertEquals("Movistar Arena", publicacion.anteriores().lugar());
    }

    @Test
    void reprogramarConLosMismosDatosNoAvisa() {
        var id = guardarEnEstado(EstadoEvento.PUBLICADO);

        casoDeUso.reprogramar(id, new ReprogramarEventoCommand(fechaOriginal, "Movistar Arena"));

        assertTrue(publicador.publicaciones().isEmpty());
    }

    @Test
    void transicionInvalidaNoGuardaNiAvisa() {
        var id = guardarEnEstado(EstadoEvento.FINALIZADO);

        assertThrows(ReglaDeNegocioException.class, () -> casoDeUso.publicar(id));

        assertEquals(EstadoEvento.FINALIZADO, estadoGuardado(id));
        assertTrue(publicador.publicaciones().isEmpty());
    }

    @Test
    void operarSobreUnEventoInexistenteLanzaNoEncontrado() {
        assertThrows(EventoNoEncontradoException.class, () -> casoDeUso.publicar(UUID.randomUUID()));
    }
}