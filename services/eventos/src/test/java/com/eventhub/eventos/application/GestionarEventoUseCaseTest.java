package com.eventhub.eventos.application;

import com.eventhub.eventos.domain.CategoriaBoleta;
import com.eventhub.eventos.domain.EstadoEvento;
import com.eventhub.eventos.domain.ReglaDeNegocioException;
import com.eventhub.eventos.domain.TipoBoleta;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GestionarEventoUseCaseTest {

    private final EventoRepositoryEnMemoria repositorio = new EventoRepositoryEnMemoria();
    private final GestionarEventoUseCase casoDeUso = new GestionarEventoUseCase(repositorio);
    private final Instant fechaFutura = Instant.now().plus(30, ChronoUnit.DAYS);

    private CrearEventoCommand datosValidos() {
        return new CrearEventoCommand("Concierto", "Movistar Arena", fechaFutura, 1000,
                UUID.randomUUID(), "org@correo.com",
                List.of(new CategoriaBoleta(TipoBoleta.GENERAL, 1000, 50_000)));
    }

    @Test
    void crearGuardaElEventoEnBorrador() {
        var creado = casoDeUso.crear(datosValidos());

        var guardado = repositorio.buscarPorId(creado.getId()).orElseThrow();
        assertEquals(EstadoEvento.BORRADOR, guardado.getEstado());
        assertEquals("Concierto", guardado.getNombre());
    }

    @Test
    void crearConDatosInvalidosNoGuardaNada() {
        var invalidos = new CrearEventoCommand("Concierto", "Movistar Arena", fechaFutura, 100,
                UUID.randomUUID(), "org@correo.com",
                List.of(new CategoriaBoleta(TipoBoleta.GENERAL, 500, 50_000))); // 500 > 100

        assertThrows(ReglaDeNegocioException.class, () -> casoDeUso.crear(invalidos));
        assertEquals(0, repositorio.cantidad());
    }

    @Test
    void editarActualizaElEventoGuardado() {
        var creado = casoDeUso.crear(datosValidos());
        var cambios = new EditarEventoCommand("Festival", "Parque Simón Bolívar", fechaFutura, 2000,
                List.of(new CategoriaBoleta(TipoBoleta.VIP, 300, 200_000)));

        casoDeUso.editar(creado.getId(), cambios);

        var guardado = repositorio.buscarPorId(creado.getId()).orElseThrow();
        assertEquals("Festival", guardado.getNombre());
        assertEquals(2000, guardado.getAforoMaximo());
    }

    @Test
    void editarEventoInexistenteLanzaNoEncontrado() {
        var cambios = new EditarEventoCommand("Festival", "Parque Simón Bolívar", fechaFutura, 2000,
                List.of(new CategoriaBoleta(TipoBoleta.VIP, 300, 200_000)));

        assertThrows(EventoNoEncontradoException.class,
                () -> casoDeUso.editar(UUID.randomUUID(), cambios));
    }
}