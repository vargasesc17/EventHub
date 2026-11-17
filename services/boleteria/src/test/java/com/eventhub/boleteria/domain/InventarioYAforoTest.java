package com.eventhub.boleteria.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class InventarioYAforoTest {

    private final UUID eventoId = UUID.randomUUID();
    private final UUID organizadorId = UUID.randomUUID();
    private final Instant fecha = Instant.parse("2030-06-01T20:00:00Z");

    private AforoEvento aforoCon(int aforoMaximo, int vendidas) {
        return new AforoEvento(eventoId, "Concierto", fecha, "Movistar Arena", organizadorId,
                "org@correo.com", aforoMaximo, vendidas, false, false);
    }

    // ----------------------------------------------------------- Inventario

    @Test
    void inventarioNuevoTieneTodoDisponible() {
        var inventario = InventarioCategoria.nuevo(eventoId, TipoBoleta.VIP, 200, 150_000);

        assertEquals(200, inventario.cupos());
        assertEquals(200, inventario.disponibles());
        assertEquals(0, inventario.reservadas());
        assertEquals(0, inventario.vendidas());
        assertEquals(150_000, inventario.precioBase());
    }

    @Test
    void inventarioRechazaContadoresQueNoSumanLosCupos() {
        assertThrows(ReglaDeNegocioException.class,
                () -> new InventarioCategoria(eventoId, TipoBoleta.VIP, 200, 150, 10, 30, 150_000));
    }

    @Test
    void inventarioRechazaDatosInvalidos() {
        assertThrows(ReglaDeNegocioException.class, () -> InventarioCategoria.nuevo(null, TipoBoleta.VIP, 200, 150_000));
        assertThrows(ReglaDeNegocioException.class, () -> InventarioCategoria.nuevo(eventoId, null, 200, 150_000));
        assertThrows(ReglaDeNegocioException.class, () -> InventarioCategoria.nuevo(eventoId, TipoBoleta.VIP, -1, 150_000));
        assertThrows(ReglaDeNegocioException.class, () -> InventarioCategoria.nuevo(eventoId, TipoBoleta.VIP, 200, -1));
    }

    // ---------------------------------------------------------------- Aforo

    @Test
    void aforoNuevoEmpiezaSinVentasYAceptandoCompras() {
        var aforo = AforoEvento.nuevo(eventoId, "Concierto", fecha, "Movistar Arena", organizadorId,
                "org@correo.com", 1000);

        assertEquals(0, aforo.vendidas());
        assertFalse(aforo.alertaEnviada());
        assertTrue(aforo.aceptaVentas());
    }

    @ParameterizedTest
    @CsvSource({"899, false", "900, true", "901, true", "1000, true"})
    void alcanzaElUmbralAlLlegarAl90PorCientoOSuperarlo(int vendidas, boolean esperado) {
        assertEquals(esperado, aforoCon(1000, vendidas).alcanzaUmbral(90));
    }

    @Test
    void conVentasBloqueadasNoAceptaCompras() {
        var bloqueado = new AforoEvento(eventoId, "Concierto", fecha, "Movistar Arena", organizadorId,
                "org@correo.com", 1000, 0, false, true);

        assertFalse(bloqueado.aceptaVentas());
    }

    @Test
    void aforoRechazaDatosInvalidos() {
        assertThrows(ReglaDeNegocioException.class, () -> aforoCon(0, 0));
        assertThrows(ReglaDeNegocioException.class, () -> aforoCon(1000, 1001));
        assertThrows(ReglaDeNegocioException.class, () -> aforoCon(1000, -1));
        assertThrows(ReglaDeNegocioException.class, () -> AforoEvento.nuevo(eventoId, " ", fecha,
                "Movistar Arena", organizadorId, "org@correo.com", 1000));
    }
}