package com.eventhub.eventos.application;

import com.eventhub.eventos.domain.CategoriaBoleta;
import com.eventhub.eventos.domain.EstadoEvento;
import com.eventhub.eventos.domain.Evento;
import com.eventhub.eventos.domain.TipoBoleta;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ConsultarEventosUseCaseTest {

    private final EventoRepositoryEnMemoria repositorio = new EventoRepositoryEnMemoria();
    private final ConsultarEventosUseCase casoDeUso = new ConsultarEventosUseCase(repositorio);
    private final Instant futuro = Instant.now().plus(30, ChronoUnit.DAYS);
    private final Instant pasado = Instant.now().minus(1, ChronoUnit.DAYS);

    private Evento guardar(String nombre, EstadoEvento estado, Instant fecha) {
        var evento = Evento.reconstruir(UUID.randomUUID(), nombre, "Movistar Arena", fecha, 1000,
                UUID.randomUUID(), "org@correo.com", estado,
                List.of(new CategoriaBoleta(TipoBoleta.GENERAL, 1000, 50_000)));
        repositorio.guardar(evento);
        return evento;
    }

    @Test
    void buscarPorIdDevuelveElEvento() {
        var evento = guardar("Concierto", EstadoEvento.BORRADOR, futuro);

        assertEquals("Concierto", casoDeUso.buscarPorId(evento.getId()).getNombre());
    }

    @Test
    void buscarPorIdInexistenteLanzaNoEncontrado() {
        assertThrows(EventoNoEncontradoException.class,
                () -> casoDeUso.buscarPorId(UUID.randomUUID()));
    }

    @Test
    void catalogoSoloIncluyePublicadosConFechaFutura() {
        guardar("Visible", EstadoEvento.PUBLICADO, futuro);
        guardar("En borrador", EstadoEvento.BORRADOR, futuro);
        guardar("Cancelado", EstadoEvento.CANCELADO, futuro);
        guardar("Ya pasó", EstadoEvento.PUBLICADO, pasado);

        var catalogo = casoDeUso.catalogo();

        assertEquals(1, catalogo.size());
        assertEquals("Visible", catalogo.get(0).getNombre());
    }
}