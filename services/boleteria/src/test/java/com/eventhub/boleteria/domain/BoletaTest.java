package com.eventhub.boleteria.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BoletaTest {

    private final UUID id = UUID.randomUUID();
    private final UUID eventoId = UUID.randomUUID();
    private final UUID compradorId = UUID.randomUUID();
    private final Instant ahora = Instant.now();

    // -------------------------------------------------------------- General

    @Test
    void boletaGeneralGuardaSusDatos() {
        var boleta = new BoletaGeneral(id, eventoId, compradorId, 50_000, EstadoBoleta.ACTIVA, ahora);

        assertEquals(TipoBoleta.GENERAL, boleta.categoria());
        assertEquals(id, boleta.getId());
        assertEquals(eventoId, boleta.getEventoId());
        assertEquals(compradorId, boleta.getCompradorId());
        assertEquals(50_000, boleta.getPrecioPagado());
        assertEquals(EstadoBoleta.ACTIVA, boleta.getEstado());
        assertEquals(ahora, boleta.getFechaCompra());
    }

    @Test
    void ningunaBoletaAceptaPrecioNegativo() {
        assertThrows(ReglaDeNegocioException.class,
                () -> new BoletaGeneral(id, eventoId, compradorId, -1, EstadoBoleta.ACTIVA, ahora));
    }

    @Test
    void ningunaBoletaAceptaDatosObligatoriosNulos() {
        assertThrows(ReglaDeNegocioException.class,
                () -> new BoletaGeneral(id, null, compradorId, 50_000, EstadoBoleta.ACTIVA, ahora));
        assertThrows(ReglaDeNegocioException.class,
                () -> new BoletaGeneral(id, eventoId, null, 50_000, EstadoBoleta.ACTIVA, ahora));
    }

    // ------------------------------------------------------------------ VIP

    @Test
    void boletaVipTieneBeneficios() {
        var boleta = new BoletaVIP(id, eventoId, compradorId, 150_000, EstadoBoleta.ACTIVA, ahora,
                List.of("Zona VIP", "Ingreso preferencial"));

        assertEquals(TipoBoleta.VIP, boleta.categoria());
        assertEquals(List.of("Zona VIP", "Ingreso preferencial"), boleta.getBeneficios());
    }

    @ParameterizedTest
    @NullAndEmptySource
    void boletaVipExigeAlMenosUnBeneficio(List<String> beneficios) {
        assertThrows(ReglaDeNegocioException.class,
                () -> new BoletaVIP(id, eventoId, compradorId, 150_000, EstadoBoleta.ACTIVA, ahora, beneficios));
    }

    @Test
    void losBeneficiosNoSePuedenModificarDesdeAfuera() {
        var lista = new ArrayList<>(List.of("Zona VIP"));
        var boleta = new BoletaVIP(id, eventoId, compradorId, 150_000, EstadoBoleta.ACTIVA, ahora, lista);

        lista.add("Camerino");

        assertEquals(1, boleta.getBeneficios().size());
        assertThrows(UnsupportedOperationException.class, () -> boleta.getBeneficios().add("Otro"));
    }

    // ----------------------------------------------------------- Estudiante

    @Test
    void boletaEstudianteGuardaElCarnet() {
        var boleta = new BoletaEstudiante(id, eventoId, compradorId, 30_000, EstadoBoleta.ACTIVA, ahora, "0000123456");

        assertEquals(TipoBoleta.ESTUDIANTE, boleta.categoria());
        assertEquals("0000123456", boleta.getCarnetEstudiantil());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void boletaEstudianteExigeCarnet(String carnet) {
        assertThrows(ReglaDeNegocioException.class,
                () -> new BoletaEstudiante(id, eventoId, compradorId, 30_000, EstadoBoleta.ACTIVA, ahora, carnet));
    }

    // ------------------------------------------------------------- Cortesía

    @Test
    void boletaCortesiaGuardaQuienLaEmitio() {
        var staff = UUID.randomUUID();
        var boleta = new BoletaCortesia(id, eventoId, compradorId, 0, EstadoBoleta.ACTIVA, ahora, staff);

        assertEquals(TipoBoleta.CORTESIA, boleta.categoria());
        assertEquals(staff, boleta.getEmitidaPor());
        assertEquals(0, boleta.getPrecioPagado());
    }

    @Test
    void boletaCortesiaExigeQuienLaEmitio() {
        assertThrows(ReglaDeNegocioException.class,
                () -> new BoletaCortesia(id, eventoId, compradorId, 0, EstadoBoleta.ACTIVA, ahora, null));
    }

    @Test
    void boletaCortesiaNoSeCobra() {
        assertThrows(ReglaDeNegocioException.class,
                () -> new BoletaCortesia(id, eventoId, compradorId, 10_000, EstadoBoleta.ACTIVA, ahora, UUID.randomUUID()));
    }
}