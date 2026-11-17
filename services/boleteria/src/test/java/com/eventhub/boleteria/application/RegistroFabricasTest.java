package com.eventhub.boleteria.application;

import com.eventhub.boleteria.domain.Boleta;
import com.eventhub.boleteria.domain.BoletaVIP;
import com.eventhub.boleteria.domain.Compra;
import com.eventhub.boleteria.domain.EstadoBoleta;
import com.eventhub.boleteria.domain.FabricaBoleta;
import com.eventhub.boleteria.domain.FabricaBoletaCortesia;
import com.eventhub.boleteria.domain.FabricaBoletaEstudiante;
import com.eventhub.boleteria.domain.FabricaBoletaGeneral;
import com.eventhub.boleteria.domain.FabricaBoletaVIP;
import com.eventhub.boleteria.domain.ReglaDeNegocioException;
import com.eventhub.boleteria.domain.TipoBoleta;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RegistroFabricasTest {

    private final RegistroFabricas registro = new RegistroFabricas(List.of(
            new FabricaBoletaGeneral(), new FabricaBoletaVIP(),
            new FabricaBoletaEstudiante(), new FabricaBoletaCortesia()));

    /** Así se verá el flujo de compra (#23): no menciona ningún tipo concreto. */
    private Boleta emitir(RegistroFabricas registro, Compra compra, long precio) {
        return registro.fabricaPara(compra.categoria()).emitir(compra, precio);
    }

    @ParameterizedTest
    @EnumSource(TipoBoleta.class)
    void hayUnaFabricaParaCadaTipo(TipoBoleta tipo) {
        assertEquals(tipo, registro.fabricaPara(tipo).tipo());
    }

    @ParameterizedTest
    @EnumSource(TipoBoleta.class)
    void elMismoFlujoEmiteCualquierTipo(TipoBoleta tipo) {
        var compra = new Compra(UUID.randomUUID(), tipo, UUID.randomUUID(), "Ana Pérez", "ana@correo.com",
                "0000123456", UUID.randomUUID());
        long precio = tipo == TipoBoleta.CORTESIA ? 0 : 50_000;

        assertEquals(tipo, emitir(registro, compra, precio).categoria());
    }

    @Test
    void pedirUnTipoSinFabricaFallaConMensajeClaro() {
        var incompleto = new RegistroFabricas(List.of(new FabricaBoletaGeneral()));

        var error = assertThrows(ReglaDeNegocioException.class, () -> incompleto.fabricaPara(TipoBoleta.VIP));

        assertTrue(error.getMessage().contains("VIP"));
    }

    @Test
    void noSePuedenRegistrarDosFabricasDelMismoTipo() {
        assertThrows(IllegalArgumentException.class,
                () -> new RegistroFabricas(List.of(new FabricaBoletaGeneral(), new FabricaBoletaGeneral())));
    }

    // ------------------------------------------------------------------ OCP

    /** Una fábrica que no existía cuando se escribieron el registro y el flujo. */
    static class FabricaVipPlatino extends FabricaBoleta {
        @Override
        public TipoBoleta tipo() { return TipoBoleta.VIP; }

        @Override
        protected Boleta crearBoleta(Compra compra, long precio) {
            return new BoletaVIP(UUID.randomUUID(), compra.eventoId(), compra.compradorId(), precio,
                    EstadoBoleta.ACTIVA, Instant.now(), List.of("Camerino", "Parqueadero"));
        }
    }

    @Test
    void unaFabricaNuevaSeUsaSinModificarElRegistroNiElFlujo() {
        var conPlatino = new RegistroFabricas(List.of(new FabricaBoletaGeneral(), new FabricaVipPlatino()));
        var compra = new Compra(UUID.randomUUID(), TipoBoleta.VIP, UUID.randomUUID(), "Ana Pérez",
                "ana@correo.com", null, null);

        var boleta = (BoletaVIP) emitir(conPlatino, compra, 300_000);

        assertEquals(List.of("Camerino", "Parqueadero"), boleta.getBeneficios());
    }
}