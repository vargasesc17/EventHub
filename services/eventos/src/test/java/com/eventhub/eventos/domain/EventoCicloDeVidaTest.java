package com.eventhub.eventos.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EventoCicloDeVidaTest {

    private static final Set<String> VALIDAS = Set.of(
            "BORRADOR->PUBLICADO", "BORRADOR->CANCELADO",
            "PUBLICADO->EN_CURSO", "PUBLICADO->CANCELADO",
            "EN_CURSO->FINALIZADO");

    private final Instant fechaFutura = Instant.now().plus(30, ChronoUnit.DAYS);

    private Evento eventoEnEstado(EstadoEvento estado) {
        return Evento.reconstruir(UUID.randomUUID(), "Concierto", "Movistar Arena", fechaFutura, 1000,
                UUID.randomUUID(), "org@correo.com", estado,
                List.of(new CategoriaBoleta(TipoBoleta.GENERAL, 1000, 50_000)));
    }

    // ------------------------------------------------- tabla de transiciones

    @ParameterizedTest
    @CsvSource({"BORRADOR,PUBLICADO", "BORRADOR,CANCELADO", "PUBLICADO,EN_CURSO",
            "PUBLICADO,CANCELADO", "EN_CURSO,FINALIZADO"})
    void transicionesPermitidas(EstadoEvento origen, EstadoEvento destino) {
        assertTrue(origen.puedePasarA(destino));
    }

    @Test
    void cualquierOtraTransicionEstaProhibida() {
        for (var origen : EstadoEvento.values()) {
            for (var destino : EstadoEvento.values()) {
                if (!VALIDAS.contains(origen + "->" + destino)) {
                    assertFalse(origen.puedePasarA(destino), origen + " -> " + destino);
                }
            }
        }
    }

    // ------------------------------------------------------------ en Evento

    @Test
    void cicloCompletoDeUnEvento() {
        var evento = eventoEnEstado(EstadoEvento.BORRADOR);

        evento.publicar();
        assertEquals(EstadoEvento.PUBLICADO, evento.getEstado());
        evento.iniciar();
        assertEquals(EstadoEvento.EN_CURSO, evento.getEstado());
        evento.finalizar();
        assertEquals(EstadoEvento.FINALIZADO, evento.getEstado());
    }

    @ParameterizedTest
    @EnumSource(value = EstadoEvento.class, names = {"BORRADOR", "PUBLICADO"})
    void seCancelaDesdeBorradorOPublicado(EstadoEvento estado) {
        var evento = eventoEnEstado(estado);

        evento.cancelar();

        assertEquals(EstadoEvento.CANCELADO, evento.getEstado());
    }

    @ParameterizedTest
    @EnumSource(value = EstadoEvento.class, names = {"EN_CURSO", "FINALIZADO", "CANCELADO"})
    void noSeCancelaUnEventoEnCursoNiTerminado(EstadoEvento estado) {
        var evento = eventoEnEstado(estado);

        assertThrows(ReglaDeNegocioException.class, evento::cancelar);
        assertEquals(estado, evento.getEstado());
    }

    @Test
    void transicionInvalidaDiceDeQueEstadoACual() {
        var evento = eventoEnEstado(EstadoEvento.BORRADOR);

        var error = assertThrows(ReglaDeNegocioException.class, evento::finalizar);

        assertTrue(error.getMessage().contains("BORRADOR"));
        assertTrue(error.getMessage().contains("FINALIZADO"));
    }

    @Test
    void noSePuedePublicarDosVeces() {
        var evento = eventoEnEstado(EstadoEvento.PUBLICADO);

        assertThrows(ReglaDeNegocioException.class, evento::publicar);
    }

    // ------------------------------------------------------- fecha y lugar

    @Test
    void cambiaFechaYLugarDeUnEventoPublicado() {
        var evento = eventoEnEstado(EstadoEvento.PUBLICADO);
        var nuevaFecha = fechaFutura.plus(2, ChronoUnit.DAYS);

        evento.cambiarFecha(nuevaFecha);
        evento.cambiarLugar("Coliseo, Chía");

        assertEquals(nuevaFecha, evento.getFechaInicio());
        assertEquals("Coliseo, Chía", evento.getLugar());
    }

    @ParameterizedTest
    @EnumSource(value = EstadoEvento.class, names = "PUBLICADO", mode = EnumSource.Mode.EXCLUDE)
    void fechaYLugarSoloCambianEnPublicado(EstadoEvento estado) {
        var evento = eventoEnEstado(estado);

        assertThrows(ReglaDeNegocioException.class, () -> evento.cambiarFecha(fechaFutura.plus(1, ChronoUnit.DAYS)));
        assertThrows(ReglaDeNegocioException.class, () -> evento.cambiarLugar("Otro lugar"));
        assertEquals(fechaFutura, evento.getFechaInicio());
        assertEquals("Movistar Arena", evento.getLugar());
    }

    @Test
    void rechazaFechaNulaYLugarVacio() {
        var evento = eventoEnEstado(EstadoEvento.PUBLICADO);

        assertThrows(ReglaDeNegocioException.class, () -> evento.cambiarFecha(null));
        assertThrows(ReglaDeNegocioException.class, () -> evento.cambiarLugar("  "));
    }
}