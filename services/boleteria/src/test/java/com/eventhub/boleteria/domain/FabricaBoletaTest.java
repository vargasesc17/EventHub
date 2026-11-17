package com.eventhub.boleteria.domain;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FabricaBoletaTest {

    private final UUID eventoId = UUID.randomUUID();
    private final UUID compradorId = UUID.randomUUID();
    private final UUID staff = UUID.randomUUID();

    private Compra compra(TipoBoleta tipo, String carnet, UUID emitidaPor) {
        return new Compra(eventoId, tipo, compradorId, "Ana Pérez", "ana@correo.com", carnet, emitidaPor);
    }

    private void assertDatosComunes(Boleta boleta, long precio) {
        assertNotNull(boleta.getId());
        assertEquals(eventoId, boleta.getEventoId());
        assertEquals(compradorId, boleta.getCompradorId());
        assertEquals(precio, boleta.getPrecioPagado());
        assertEquals(EstadoBoleta.ACTIVA, boleta.getEstado());
        assertNotNull(boleta.getFechaCompra());
    }

    @Test
    void fabricaGeneralEmiteBoletaGeneral() {
        var boleta = new FabricaBoletaGeneral().emitir(compra(TipoBoleta.GENERAL, null, null), 50_000);

        assertInstanceOf(BoletaGeneral.class, boleta);
        assertDatosComunes(boleta, 50_000);
    }

    @Test
    void fabricaVipEmiteBoletaVipConSusBeneficios() {
        var boleta = new FabricaBoletaVIP().emitir(compra(TipoBoleta.VIP, null, null), 150_000);

        var vip = assertInstanceOf(BoletaVIP.class, boleta);
        assertDatosComunes(boleta, 150_000);
        assertFalse(vip.getBeneficios().isEmpty());
    }

    @Test
    void fabricaEstudianteEmiteBoletaConElCarnet() {
        var boleta = new FabricaBoletaEstudiante().emitir(compra(TipoBoleta.ESTUDIANTE, "0000123456", null), 30_000);

        var estudiante = assertInstanceOf(BoletaEstudiante.class, boleta);
        assertDatosComunes(boleta, 30_000);
        assertEquals("0000123456", estudiante.getCarnetEstudiantil());
    }

    @Test
    void fabricaEstudianteRechazaCompraSinCarnet() {
        assertThrows(ReglaDeNegocioException.class,
                () -> new FabricaBoletaEstudiante().emitir(compra(TipoBoleta.ESTUDIANTE, null, null), 30_000));
    }

    @Test
    void fabricaCortesiaEmiteBoletaGratisConQuienLaEmitio() {
        var boleta = new FabricaBoletaCortesia().emitir(compra(TipoBoleta.CORTESIA, null, staff), 0);

        var cortesia = assertInstanceOf(BoletaCortesia.class, boleta);
        assertDatosComunes(boleta, 0);
        assertEquals(staff, cortesia.getEmitidaPor());
    }

    @Test
    void fabricaCortesiaRechazaCompraSinEmisorOConPrecio() {
        var fabrica = new FabricaBoletaCortesia();

        assertThrows(ReglaDeNegocioException.class,
                () -> fabrica.emitir(compra(TipoBoleta.CORTESIA, null, null), 0));
        assertThrows(ReglaDeNegocioException.class,
                () -> fabrica.emitir(compra(TipoBoleta.CORTESIA, null, staff), 10_000));
    }

    @Test
    void unaFabricaNoEmiteBoletasDeOtroTipo() {
        var error = assertThrows(ReglaDeNegocioException.class,
                () -> new FabricaBoletaGeneral().emitir(compra(TipoBoleta.VIP, null, null), 150_000));

        assertTrue(error.getMessage().contains("GENERAL"));
        assertTrue(error.getMessage().contains("VIP"));
    }

    @Test
    void cadaBoletaEmitidaTieneUnIdDistinto() {
        var fabrica = new FabricaBoletaGeneral();
        var compra = compra(TipoBoleta.GENERAL, null, null);

        assertNotEquals(fabrica.emitir(compra, 50_000).getId(), fabrica.emitir(compra, 50_000).getId());
    }

    @Test
    void laCompraExigeEventoCategoriaYComprador() {
        assertThrows(ReglaDeNegocioException.class,
                () -> new Compra(null, TipoBoleta.GENERAL, compradorId, "Ana", "ana@correo.com", null, null));
        assertThrows(ReglaDeNegocioException.class,
                () -> new Compra(eventoId, null, compradorId, "Ana", "ana@correo.com", null, null));
    }
}