package com.eventhub.boleteria.application;

import com.eventhub.boleteria.domain.AforoEvento;
import com.eventhub.boleteria.domain.BoletaGeneral;
import com.eventhub.boleteria.domain.EstadoBoleta;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/** "Mis boletas": las del comprador, con los datos del evento y el QR solo si siguen sirviendo. */
class ConsultarBoletasDelCompradorUseCaseTest {

    private final BoletasEnMemoria boletas = new BoletasEnMemoria();
    private final AforosEnMemoria aforos = new AforosEnMemoria();
    private final ConsultarBoletasDelCompradorUseCase consultar =
            new ConsultarBoletasDelCompradorUseCase(boletas, aforos, id -> "qr-" + id);

    private final UUID compradorId = UUID.randomUUID();
    private final UUID eventoId = UUID.randomUUID();

    private BoletaGeneral boleta(UUID comprador, EstadoBoleta estado, String fechaCompra) {
        return new BoletaGeneral(UUID.randomUUID(), eventoId, comprador, 50_000, estado, Instant.parse(fechaCompra));
    }

    @Test
    void devuelveSoloLasDelCompradorDeLaMasRecienteALaMasAntigua() {
        aforos.crear(AforoEvento.nuevo(eventoId, "Concierto", Instant.parse("2030-06-01T20:00:00Z"),
                "Movistar Arena", UUID.randomUUID(), "org@correo.com", 100));
        var antigua = boleta(compradorId, EstadoBoleta.ACTIVA, "2030-05-01T10:00:00Z");
        var reciente = boleta(compradorId, EstadoBoleta.ACTIVA, "2030-05-02T10:00:00Z");
        boletas.guardar(antigua);
        boletas.guardar(reciente);
        boletas.guardar(boleta(UUID.randomUUID(), EstadoBoleta.ACTIVA, "2030-05-03T10:00:00Z"));

        var resultado = consultar.consultar(compradorId);

        assertEquals(2, resultado.size());
        assertEquals(reciente.getId(), resultado.get(0).boleta().getId());
        assertEquals(antigua.getId(), resultado.get(1).boleta().getId());
        assertEquals("Concierto", resultado.get(0).evento().nombre());
        assertEquals("qr-" + reciente.getId(), resultado.get(0).codigoQr());
    }

    @Test
    void elQrSoloVieneEnLasBoletasActivas() {
        boletas.guardar(boleta(compradorId, EstadoBoleta.USADA, "2030-05-01T10:00:00Z"));
        boletas.guardar(boleta(compradorId, EstadoBoleta.CANCELADA, "2030-05-02T10:00:00Z"));

        consultar.consultar(compradorId).forEach(item -> assertNull(item.codigoQr(),
                "una boleta " + item.boleta().getEstado() + " no sirve para entrar"));
    }

    @Test
    void siBoleteriaNoConoceElEventoLaBoletaIgualAparece() {
        boletas.guardar(boleta(compradorId, EstadoBoleta.ACTIVA, "2030-05-01T10:00:00Z"));

        var resultado = consultar.consultar(compradorId);

        assertEquals(1, resultado.size());
        assertNull(resultado.get(0).evento());
    }

    @Test
    void unCompradorSinComprasRecibeUnaListaVacia() {
        assertTrue(consultar.consultar(UUID.randomUUID()).isEmpty());
    }

    @Test
    void sinCompradorNoSePuedeConsultar() {
        assertThrows(IllegalArgumentException.class, () -> consultar.consultar(null));
    }
}
