package com.eventhub.boleteria.application;

import com.eventhub.boleteria.domain.BoletaGeneral;
import com.eventhub.boleteria.domain.EstadoBoleta;
import com.eventhub.boleteria.domain.TipoBoleta;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SincronizarEventoUseCaseTest {

    private final MensajesEnMemoria mensajes = new MensajesEnMemoria();
    private final AforosEnMemoria aforos = new AforosEnMemoria();
    private final InventariosEnMemoria inventarios = new InventariosEnMemoria();
    private final BoletasEnMemoria boletas = new BoletasEnMemoria();

    private final SincronizarEventoUseCase casoDeUso = new SincronizarEventoUseCase(mensajes, aforos,
            inventarios, boletas, Clock.fixed(Instant.parse("2030-01-01T00:00:00Z"), ZoneOffset.UTC));  private final UUID eventoId = UUID.randomUUID();
    private final Instant fecha = Instant.parse("2030-06-01T20:00:00Z");

    private EventoActualizadoComando comando(Set<TipoCambio> cambios, Instant fechaInicio, String lugar) {
        return new EventoActualizadoComando(UUID.randomUUID(), eventoId, cambios, "Concierto", fechaInicio, lugar,
                1000, UUID.randomUUID(), "org@correo.com",
                List.of(new EventoActualizadoComando.Categoria(TipoBoleta.GENERAL, 800, 50_000),
                        new EventoActualizadoComando.Categoria(TipoBoleta.VIP, 200, 150_000)));
    }

    private EventoActualizadoComando publicado() {
        return comando(Set.of(TipoCambio.PUBLICADO), fecha, "Movistar Arena");
    }

    @Test
    void publicadoCreaLaCopiaDelEventoYSuInventario() {
        assertEquals(Resultado.PROCESADO, casoDeUso.ejecutar(publicado()));

        var aforo = aforos.buscar(eventoId).orElseThrow();
        assertEquals("Concierto", aforo.nombre());
        assertEquals(1000, aforo.aforoMaximo());
        assertEquals("org@correo.com", aforo.organizadorCorreo());
        assertTrue(aforo.aceptaVentas());

        var inventario = inventarios.buscarPorEvento(eventoId);
        assertEquals(2, inventario.size());
        assertEquals(800, inventario.get(0).disponibles());
        assertEquals(50_000, inventario.get(0).precioBase());
        assertEquals(200, inventario.get(1).disponibles());
    }

    @Test
    void elMismoMensajeDosVecesSoloSeAplicaUnaVez() {
        var mensaje = publicado();

        assertEquals(Resultado.PROCESADO, casoDeUso.ejecutar(mensaje));
        assertEquals(Resultado.DUPLICADO, casoDeUso.ejecutar(mensaje));

        assertEquals(2, inventarios.buscarPorEvento(eventoId).size());
    }

    @Test
    void otroPublicadoDelMismoEventoNoDuplicaElInventario() {
        casoDeUso.ejecutar(publicado());

        assertEquals(Resultado.IGNORADO, casoDeUso.ejecutar(publicado()));

        assertEquals(2, inventarios.buscarPorEvento(eventoId).size());
    }

    @Test
    void fechaYLugarActualizanLaCopiaSinTocarElInventario() {
        casoDeUso.ejecutar(publicado());
        var nuevaFecha = Instant.parse("2030-06-03T20:00:00Z");

        var resultado = casoDeUso.ejecutar(comando(Set.of(TipoCambio.FECHA, TipoCambio.LUGAR), nuevaFecha, "Coliseo, Chía"));

        assertEquals(Resultado.PROCESADO, resultado);
        var aforo = aforos.buscar(eventoId).orElseThrow();
        assertEquals(nuevaFecha, aforo.fechaInicio());
        assertEquals("Coliseo, Chía", aforo.lugar());
        assertEquals(2, inventarios.buscarPorEvento(eventoId).size());
    }

    @Test
    void canceladoBloqueaLasVentas() {
        casoDeUso.ejecutar(publicado());

        assertEquals(Resultado.PROCESADO, casoDeUso.ejecutar(comando(Set.of(TipoCambio.CANCELADO), fecha, "Movistar Arena")));

        assertFalse(aforos.buscar(eventoId).orElseThrow().aceptaVentas());
    }

    @Test
    void canceladoDeUnEventoDesconocidoSeIgnora() {
        assertEquals(Resultado.IGNORADO, casoDeUso.ejecutar(comando(Set.of(TipoCambio.CANCELADO), fecha, "Movistar Arena")));

        assertTrue(aforos.buscar(eventoId).isEmpty());
    }

    @Test
    void unCambioDeUnEventoDesconocidoLoCreaConElEstadoCompleto() {
        var resultado = casoDeUso.ejecutar(comando(Set.of(TipoCambio.LUGAR), fecha, "Coliseo, Chía"));

        assertEquals(Resultado.PROCESADO, resultado);
        assertEquals("Coliseo, Chía", aforos.buscar(eventoId).orElseThrow().lugar());
        assertEquals(2, inventarios.buscarPorEvento(eventoId).size());
    }

    @Test
    void inventarioDeUnEventoConocidoSeIgnoraPeroQuedaRegistrado() {
        casoDeUso.ejecutar(publicado());
        var mensaje = comando(Set.of(TipoCambio.INVENTARIO), fecha, "Movistar Arena");

        assertEquals(Resultado.IGNORADO, casoDeUso.ejecutar(mensaje));
        assertEquals(Resultado.DUPLICADO, casoDeUso.ejecutar(mensaje));
    }

    @Test
    void canceladoCancelaLasBoletasVendidasDelEvento() {
        casoDeUso.ejecutar(publicado());
        var deEsteEvento = new BoletaGeneral(UUID.randomUUID(), eventoId, UUID.randomUUID(), 50_000,
                EstadoBoleta.ACTIVA, Instant.now());
        var deOtroEvento = new BoletaGeneral(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), 50_000,
                EstadoBoleta.ACTIVA, Instant.now());
        boletas.guardar(deEsteEvento);
        boletas.guardar(deOtroEvento);

        casoDeUso.ejecutar(comando(Set.of(TipoCambio.CANCELADO), fecha, "Movistar Arena"));

        assertEquals(Set.of(deEsteEvento.getId()), boletas.canceladas());
    }
}