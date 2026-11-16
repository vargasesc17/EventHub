package com.eventhub.boleteria.application;

import com.eventhub.boleteria.domain.ContextoPrecio;
import com.eventhub.boleteria.domain.ParametrosPrecio;
import com.eventhub.boleteria.domain.PrecioDinamico;
import com.eventhub.boleteria.domain.PrecioEarlyBird;
import com.eventhub.boleteria.domain.PrecioFijo;
import com.eventhub.boleteria.domain.PrecioStrategy;
import com.eventhub.boleteria.domain.ReglaDeNegocioException;
import com.eventhub.boleteria.domain.TipoPrecio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RegistroEstrategiasTest {

    private final RegistroEstrategias registro = new RegistroEstrategias(
            List.of(new PrecioFijo(), new PrecioEarlyBird(), new PrecioDinamico()));

    private final ContextoPrecio contexto =
            new ContextoPrecio(80_000, 100, 0, Instant.now(), ParametrosPrecio.ninguno());

    /** Así calculará el precio el flujo de compra (#23): solo conoce la interfaz PrecioStrategy. */
    private long precio(RegistroEstrategias registro, TipoPrecio tipo, ContextoPrecio contexto) {
        PrecioStrategy estrategia = registro.estrategiaPara(tipo);
        return estrategia.calcular(contexto);
    }

    @ParameterizedTest
    @EnumSource(TipoPrecio.class)
    void hayUnaEstrategiaParaCadaTipo(TipoPrecio tipo) {
        assertEquals(tipo, registro.estrategiaPara(tipo).tipo());
    }

    @Test
    void pedirUnTipoSinEstrategiaFallaConMensajeClaro() {
        var incompleto = new RegistroEstrategias(List.of(new PrecioFijo()));

        var error = assertThrows(ReglaDeNegocioException.class,
                () -> incompleto.estrategiaPara(TipoPrecio.DINAMICO));

        assertTrue(error.getMessage().contains("DINAMICO"));
    }

    @Test
    void noSePuedenRegistrarDosEstrategiasDelMismoTipo() {
        assertThrows(IllegalArgumentException.class,
                () -> new RegistroEstrategias(List.of(new PrecioFijo(), new PrecioFijo())));
    }

    /** Una estrategia que no existía cuando se escribieron el registro y el flujo. */
    static class PrecioMitad implements PrecioStrategy {
        @Override
        public TipoPrecio tipo() { return TipoPrecio.FIJO; }

        @Override
        public long calcular(ContextoPrecio contexto) { return contexto.precioBase() / 2; }
    }

    @Test
    void unaEstrategiaNuevaSeUsaSinModificarElFlujo() {
        var conMitad = new RegistroEstrategias(List.of(new PrecioMitad()));

        assertEquals(80_000, precio(registro, TipoPrecio.FIJO, contexto));
        assertEquals(40_000, precio(conMitad, TipoPrecio.FIJO, contexto));
    }
}