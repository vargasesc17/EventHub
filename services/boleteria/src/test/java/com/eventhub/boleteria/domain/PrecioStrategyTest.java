package com.eventhub.boleteria.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PrecioStrategyTest {

    private final Instant limite = Instant.parse("2030-05-01T00:00:00Z");
    private final Instant antes = limite.minus(1, ChronoUnit.DAYS);
    private final Instant despues = limite.plus(1, ChronoUnit.DAYS);

    private ContextoPrecio contexto(long precioBase, int cupos, int vendidas, Instant fecha, ParametrosPrecio parametros) {
        return new ContextoPrecio(precioBase, cupos, vendidas, fecha, parametros);
    }

    // ----------------------------------------------------------------- Fijo

    @Test
    void precioFijoDevuelveElPrecioBase() {
        var precio = new PrecioFijo().calcular(contexto(80_000, 100, 99, antes, ParametrosPrecio.ninguno()));

        assertEquals(80_000, precio);
    }

    // ----------------------------------------------------------- Early bird

    @Test
    void earlyBirdAplicaElDescuentoAntesDeLaFechaLimite() {
        var precio = new PrecioEarlyBird().calcular(
                contexto(80_000, 100, 0, antes, ParametrosPrecio.earlyBird(limite, 25)));

        assertEquals(60_000, precio);
    }

    @Test
    void earlyBirdCobraElPrecioBaseDespuesDeLaFechaLimite() {
        var precio = new PrecioEarlyBird().calcular(
                contexto(80_000, 100, 0, despues, ParametrosPrecio.earlyBird(limite, 25)));

        assertEquals(80_000, precio);
    }

    @Test
    void earlyBirdEnElInstanteLimiteYaNoDescuenta() {
        var precio = new PrecioEarlyBird().calcular(
                contexto(80_000, 100, 0, limite, ParametrosPrecio.earlyBird(limite, 25)));

        assertEquals(80_000, precio);
    }

    @Test
    void earlyBirdExigeFechaLimiteYDescuento() {
        var estrategia = new PrecioEarlyBird();

        assertThrows(ReglaDeNegocioException.class,
                () -> estrategia.calcular(contexto(80_000, 100, 0, antes, ParametrosPrecio.ninguno())));
        assertThrows(ReglaDeNegocioException.class,
                () -> estrategia.calcular(contexto(80_000, 100, 0, antes, new ParametrosPrecio(limite, null, null))));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -10, 101})
    void earlyBirdRechazaDescuentosFueraDeRango(int descuento) {
        assertThrows(ReglaDeNegocioException.class, () -> new PrecioEarlyBird().calcular(
                contexto(80_000, 100, 0, antes, ParametrosPrecio.earlyBird(limite, descuento))));
    }

    // ------------------------------------------------------------- Dinámico

    @ParameterizedTest
    @CsvSource({"0, 100000", "50, 125000", "100, 150000"})
    void dinamicoSubeConLaOcupacion(int vendidas, long esperado) {
        var precio = new PrecioDinamico().calcular(
                contexto(100_000, 100, vendidas, antes, ParametrosPrecio.dinamico(50)));

        assertEquals(esperado, precio);
    }

    @Test
    void dinamicoRedondeaHaciaAbajoAlPesoEntero() {
        // 99.999 × (300 + 10) ÷ 300 = 103.332,3
        var precio = new PrecioDinamico().calcular(
                contexto(99_999, 3, 1, antes, ParametrosPrecio.dinamico(10)));

        assertEquals(103_332, precio);
    }

    @Test
    void dinamicoExigeUnIncrementoMayorQueCero() {
        var estrategia = new PrecioDinamico();

        assertThrows(ReglaDeNegocioException.class,
                () -> estrategia.calcular(contexto(100_000, 100, 50, antes, ParametrosPrecio.ninguno())));
        assertThrows(ReglaDeNegocioException.class,
                () -> estrategia.calcular(contexto(100_000, 100, 50, antes, ParametrosPrecio.dinamico(0))));
    }

    // -------------------------------------------------------------- Comunes

    @Test
    void conPrecioBaseCeroTodasLasEstrategiasDanCero() {
        // Una boleta de cortesía (precio base 0) sale gratis con cualquier estrategia.
        var parametros = new ParametrosPrecio(limite, 25, 50);
        var contexto = contexto(0, 100, 50, antes, parametros);

        for (PrecioStrategy estrategia : List.of(new PrecioFijo(), new PrecioEarlyBird(), new PrecioDinamico())) {
            assertEquals(0, estrategia.calcular(contexto), estrategia.tipo().toString());
        }
    }

    @Test
    void cadaEstrategiaDiceSuTipo() {
        assertEquals(TipoPrecio.FIJO, new PrecioFijo().tipo());
        assertEquals(TipoPrecio.EARLY_BIRD, new PrecioEarlyBird().tipo());
        assertEquals(TipoPrecio.DINAMICO, new PrecioDinamico().tipo());
    }

    @Test
    void elContextoRechazaDatosInvalidos() {
        var parametros = ParametrosPrecio.ninguno();

        assertThrows(ReglaDeNegocioException.class, () -> contexto(-1, 100, 0, antes, parametros));
        assertThrows(ReglaDeNegocioException.class, () -> contexto(80_000, 0, 0, antes, parametros));
        assertThrows(ReglaDeNegocioException.class, () -> contexto(80_000, 100, 101, antes, parametros));
        assertThrows(ReglaDeNegocioException.class, () -> contexto(80_000, 100, -1, antes, parametros));
        assertThrows(ReglaDeNegocioException.class, () -> contexto(80_000, 100, 0, null, parametros));
        assertThrows(ReglaDeNegocioException.class, () -> contexto(80_000, 100, 0, antes, null));
    }
}