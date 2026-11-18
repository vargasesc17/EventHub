package com.eventhub.eventos.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class CategoriaBoletaTest {

    @Test
    void creaCategoriaValida() {
        var categoria = new CategoriaBoleta(TipoBoleta.VIP, 100, 150_000);

        assertEquals(TipoBoleta.VIP, categoria.tipo());
        assertEquals(100, categoria.cupos());
        assertEquals(150_000, categoria.precioBase());
    }

    @Test
    void aceptaCortesiaConPrecioCero() {
        assertDoesNotThrow(() -> new CategoriaBoleta(TipoBoleta.CORTESIA, 20, 0));
    }

    @Test
    void rechazaTipoNulo() {
        var error = assertThrows(ReglaDeNegocioException.class,
                () -> new CategoriaBoleta(null, 100, 50_000));

        assertTrue(error.getMessage().contains("tipo"));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -100})
    void rechazaCuposQueNoSeanPositivos(int cupos) {
        assertThrows(ReglaDeNegocioException.class,
                () -> new CategoriaBoleta(TipoBoleta.GENERAL, cupos, 50_000));
    }

    @Test
    void rechazaPrecioNegativo() {
        assertThrows(ReglaDeNegocioException.class,
                () -> new CategoriaBoleta(TipoBoleta.GENERAL, 100, -1));
    }

    @Test
    void dosCategoriasConLosMismosValoresSonIguales() {
        var a = new CategoriaBoleta(TipoBoleta.VIP, 100, 150_000);
        var b = new CategoriaBoleta(TipoBoleta.VIP, 100, 150_000);

        assertEquals(a, b);
    }

    @Test
    void cortesiaConPrecioSeRechaza() {
        assertThrows(ReglaDeNegocioException.class,
                () -> new CategoriaBoleta(TipoBoleta.CORTESIA, 20, 10_000));
    }
}