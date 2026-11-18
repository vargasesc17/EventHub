package com.eventhub.boleteria.application;

import com.eventhub.boleteria.domain.AforoEvento;
import com.eventhub.boleteria.domain.BoletaCortesia;
import com.eventhub.boleteria.domain.Compra;
import com.eventhub.boleteria.domain.FabricaBoletaCortesia;
import com.eventhub.boleteria.domain.FabricaBoletaEstudiante;
import com.eventhub.boleteria.domain.FabricaBoletaGeneral;
import com.eventhub.boleteria.domain.FabricaBoletaVIP;
import com.eventhub.boleteria.domain.InventarioCategoria;
import com.eventhub.boleteria.domain.ParametrosPrecio;
import com.eventhub.boleteria.domain.PrecioDinamico;
import com.eventhub.boleteria.domain.PrecioEarlyBird;
import com.eventhub.boleteria.domain.PrecioFijo;
import com.eventhub.boleteria.domain.ReglaDeNegocioException;
import com.eventhub.boleteria.domain.TipoBoleta;
import com.eventhub.boleteria.domain.TipoPrecio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static com.eventhub.boleteria.domain.TipoBoleta.*;
import static org.junit.jupiter.api.Assertions.*;

class ComprarBoletaUseCaseTest {

    private static final Instant AHORA = Instant.parse("2030-01-01T00:00:00Z");
    private static final Instant FECHA_EVENTO = Instant.parse("2030-06-01T20:00:00Z");

    private final AforosEnMemoria aforos = new AforosEnMemoria();
    private final InventariosEnMemoria inventarios = new InventariosEnMemoria();
    private final BoletasEnMemoria boletas = new BoletasEnMemoria();
    private final PasarelaFalsa pasarela = new PasarelaFalsa();
    private final PublicadorEnMemoria publicador = new PublicadorEnMemoria();

    private final ComprarBoletaUseCase casoDeUso = new ComprarBoletaUseCase(aforos, inventarios, boletas,
            new RegistroEstrategias(List.of(new PrecioFijo(), new PrecioEarlyBird(), new PrecioDinamico())),
            new RegistroFabricas(List.of(new FabricaBoletaGeneral(), new FabricaBoletaVIP(),
                    new FabricaBoletaEstudiante(), new FabricaBoletaCortesia())),
            pasarela, boletaId -> "qr-" + boletaId, publicador, Clock.fixed(AHORA, ZoneOffset.UTC), 90);

    private final UUID eventoId = UUID.randomUUID();

    /** Aforo 10 = 6 General + 2 VIP + 1 Estudiante + 1 Cortesía. */
    @BeforeEach
    void eventoALaVenta() {
        aforos.crear(aforo(eventoId, 10));
        inventarios.crear(List.of(
                InventarioCategoria.nuevo(eventoId, GENERAL, 6, 50_000),
                InventarioCategoria.nuevo(eventoId, VIP, 2, 150_000),
                InventarioCategoria.nuevo(eventoId, ESTUDIANTE, 1, 30_000),
                InventarioCategoria.nuevo(eventoId, CORTESIA, 1, 0)));
    }

    private AforoEvento aforo(UUID id, int aforoMaximo) {
        return AforoEvento.nuevo(id, "Concierto", FECHA_EVENTO, "Movistar Arena", UUID.randomUUID(),
                "org@correo.com", aforoMaximo);
    }

    private Compra compra(TipoBoleta tipo) {
        return new Compra(eventoId, tipo, UUID.randomUUID(), "Ana Pérez", "ana@correo.com",
                "0000123456", UUID.randomUUID());
    }

    private InventarioCategoria inventario(TipoBoleta tipo) {
        return inventarios.buscar(eventoId, tipo).orElseThrow();
    }

    @Test
    void unaCompraDescuentaCobraGuardaYAvisa() {
        var resultado = casoDeUso.comprar(compra(VIP));

        var boleta = resultado.boleta();
        assertEquals(VIP, boleta.categoria());
        assertEquals(150_000, boleta.getPrecioPagado());
        assertEquals("qr-" + boleta.getId(), resultado.codigoQr());
        assertEquals(1, inventario(VIP).disponibles());
        assertEquals(1, inventario(VIP).vendidas());
        assertEquals(1, aforos.buscar(eventoId).orElseThrow().vendidas());
        assertEquals(List.of(boleta), boletas.guardadas());
        assertEquals(List.of(150_000L), pasarela.cobros());
        assertEquals(List.of(boleta), publicador.compras());
        assertTrue(publicador.alertas().isEmpty());
    }

    @Test
    void elPrecioSaleDeLaEstrategiaDeLaCategoria() {
        var otro = UUID.randomUUID();
        aforos.crear(aforo(otro, 100));
        inventarios.crear(List.of(new InventarioCategoria(otro, GENERAL, 100, 100, 0, 0, 80_000,
                TipoPrecio.EARLY_BIRD, ParametrosPrecio.earlyBird(AHORA.plusSeconds(3600), 25))));

        var resultado = casoDeUso.comprar(new Compra(otro, GENERAL, UUID.randomUUID(), "Ana Pérez",
                "ana@correo.com", null, null));

        assertEquals(60_000, resultado.boleta().getPrecioPagado());
        assertEquals(List.of(60_000L), pasarela.cobros());
    }

    @Test
    void unEventoDesconocidoLanzaNoEncontrado() {
        var compra = new Compra(UUID.randomUUID(), GENERAL, UUID.randomUUID(), "Ana", "ana@correo.com", null, null);

        assertThrows(EventoNoEncontradoException.class, () -> casoDeUso.comprar(compra));
    }

    @Test
    void unEventoCanceladoNoVende() {
        aforos.bloquearVentas(eventoId);

        assertThrows(ReglaDeNegocioException.class, () -> casoDeUso.comprar(compra(VIP)));

        assertEquals(2, inventario(VIP).disponibles());
        assertTrue(pasarela.cobros().isEmpty());
    }

    @Test
    void unaCategoriaQueElEventoNoVendeSeRechaza() {
        var otro = UUID.randomUUID();
        aforos.crear(aforo(otro, 100));
        inventarios.crear(List.of(InventarioCategoria.nuevo(otro, GENERAL, 100, 50_000)));

        assertThrows(ReglaDeNegocioException.class, () -> casoDeUso.comprar(
                new Compra(otro, VIP, UUID.randomUUID(), "Ana", "ana@correo.com", null, null)));
    }

    @Test
    void sinCuposLaCompraSeRechaza() {
        casoDeUso.comprar(compra(ESTUDIANTE));

        assertThrows(SinCuposException.class, () -> casoDeUso.comprar(compra(ESTUDIANTE)));

        assertEquals(1, boletas.guardadas().size());
        assertEquals(0, inventario(ESTUDIANTE).disponibles());
    }

    @Test
    void unPagoRechazadoNoGuardaLaBoletaNiAvisa() {
        pasarela.rechazar();

        assertThrows(PagoRechazadoException.class, () -> casoDeUso.comprar(compra(VIP)));

        assertTrue(boletas.guardadas().isEmpty());
        assertTrue(publicador.compras().isEmpty());
        // El cupo descontado lo devuelve la transacción: se comprueba en el test de integración.
    }

    @Test
    void laCortesiaNoPasaPorLaPasarela() {
        var resultado = casoDeUso.comprar(compra(CORTESIA));

        assertInstanceOf(BoletaCortesia.class, resultado.boleta());
        assertEquals(0, resultado.boleta().getPrecioPagado());
        assertTrue(pasarela.cobros().isEmpty());
    }

    @Test
    void unaBoletaInvalidaNoConsumeCupo() {
        var sinCarnet = new Compra(eventoId, ESTUDIANTE, UUID.randomUUID(), "Ana", "ana@correo.com", null, null);

        assertThrows(ReglaDeNegocioException.class, () -> casoDeUso.comprar(sinCarnet));

        assertEquals(1, inventario(ESTUDIANTE).disponibles());
        assertTrue(pasarela.cobros().isEmpty());
    }

    @Test
    void laAlertaDel90PorCientoSePublicaUnaSolaVez() {
        for (int i = 0; i < 6; i++) {
            casoDeUso.comprar(compra(GENERAL));
        }
        casoDeUso.comprar(compra(VIP));
        casoDeUso.comprar(compra(VIP));
        assertTrue(publicador.alertas().isEmpty(), "8 de 10 todavía no llega al 90 %");

        casoDeUso.comprar(compra(ESTUDIANTE));
        assertEquals(1, publicador.alertas().size(), "9 de 10 es exactamente el 90 %");
        assertEquals(9, publicador.alertas().get(0).vendidas());

        casoDeUso.comprar(compra(CORTESIA));
        assertEquals(1, publicador.alertas().size(), "la décima compra no repite la alerta");
    }
}