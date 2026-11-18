package com.eventhub.boleteria;

import com.eventhub.boleteria.application.AforoRepository;
import com.eventhub.boleteria.application.BoletaRepository;
import com.eventhub.boleteria.application.ComprarBoletaUseCase;
import com.eventhub.boleteria.application.GeneradorQr;
import com.eventhub.boleteria.application.InventarioRepository;
import com.eventhub.boleteria.application.PagoRechazadoException;
import com.eventhub.boleteria.application.PasarelaPagos;
import com.eventhub.boleteria.application.PublicadorEnMemoria;
import com.eventhub.boleteria.application.RegistroEstrategias;
import com.eventhub.boleteria.application.RegistroFabricas;
import com.eventhub.boleteria.application.SinCuposException;
import com.eventhub.boleteria.domain.AforoEvento;
import com.eventhub.boleteria.domain.Compra;
import com.eventhub.boleteria.domain.InventarioCategoria;
import com.eventhub.boleteria.infrastructure.persistencia.BoletaJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static com.eventhub.boleteria.domain.TipoBoleta.GENERAL;
import static org.junit.jupiter.api.Assertions.*;

/**
 * RNF-01 y ADR-0003 contra una base de datos real (H2). SIN @Transactional en la clase:
 * cada compra debe confirmarse de verdad, igual que en producción.
 */
@SpringBootTest
@ActiveProfiles("test")
class CompraConcurrenteTest {

    private static final int CUPOS = 10;
    private static final int COMPRADORES = 100;

    @Autowired private ComprarBoletaUseCase comprar;
    @Autowired private TransactionTemplate transaccion;
    @Autowired private AforoRepository aforos;
    @Autowired private InventarioRepository inventarios;
    @Autowired private BoletaRepository boletas;
    @Autowired private BoletaJpaRepository boletasJpa;
    @Autowired private RegistroEstrategias estrategias;
    @Autowired private RegistroFabricas fabricas;
    @Autowired private GeneradorQr generadorQr;
    @Autowired private PublicadorEnMemoria publicador;

    /** Cada test usa su propio evento: así no se estorban aunque compartan la base. */
    private UUID eventoConCupos(int cupos) {
        var eventoId = UUID.randomUUID();
        aforos.crear(AforoEvento.nuevo(eventoId, "Concierto", Instant.parse("2030-06-01T20:00:00Z"),
                "Movistar Arena", UUID.randomUUID(), "org@correo.com", cupos));
        inventarios.crear(List.of(InventarioCategoria.nuevo(eventoId, GENERAL, cupos, 50_000)));
        return eventoId;
    }

    private Compra compraGeneral(UUID eventoId) {
        return new Compra(eventoId, GENERAL, UUID.randomUUID(), "Comprador", "comprador@correo.com", null, null);
    }

    @Test
    void cienCompradoresPorDiezCuposNuncaVendenDeMas() throws Exception {
        var eventoId = eventoConCupos(CUPOS);
        var largada = new CountDownLatch(1);
        var exitosas = new AtomicInteger();
        var sinCupo = new AtomicInteger();
        var otrosErrores = new ConcurrentLinkedQueue<Throwable>();

        try (var hilos = Executors.newFixedThreadPool(COMPRADORES)) {
            for (int i = 0; i < COMPRADORES; i++) {
                hilos.submit(() -> {
                    largada.await();   // todos esperan aquí y salen al mismo tiempo
                    try {
                        transaccion.execute(estado -> comprar.comprar(compraGeneral(eventoId)));
                        exitosas.incrementAndGet();
                    } catch (SinCuposException e) {
                        sinCupo.incrementAndGet();
                    } catch (Throwable e) {
                        otrosErrores.add(e);
                    }
                    return null;
                });
            }
            largada.countDown();
        }   // al cerrar, espera a que terminen los 100

        assertTrue(otrosErrores.isEmpty(), () -> "Errores inesperados: " + otrosErrores);
        assertEquals(CUPOS, exitosas.get(), "exactamente tantas compras como cupos");
        assertEquals(COMPRADORES - CUPOS, sinCupo.get(), "los demás reciben 'sin cupos'");

        var inventario = inventarios.buscar(eventoId, GENERAL).orElseThrow();
        assertEquals(0, inventario.disponibles());
        assertEquals(CUPOS, inventario.vendidas());
        assertEquals(CUPOS, aforos.buscar(eventoId).orElseThrow().vendidas());
        assertEquals(CUPOS, boletasJpa.countByEventoId(eventoId), "ni una boleta de más en la base");
        assertEquals(1, publicador.alertas().stream().filter(a -> a.eventoId().equals(eventoId)).count(),
                "la alerta del 90 % sale una sola vez aunque varias compras la crucen a la vez");
    }

    @Test
    void siElPagoSeRechazaElCupoVuelveAlInventario() {
        var eventoId = eventoConCupos(1);
        // Repositorios reales, pero una pasarela que siempre rechaza: posible gracias a los puertos (DIP).
        PasarelaPagos rechaza = (comprador, monto, concepto) -> PasarelaPagos.ResultadoPago.rechazado("Fondos insuficientes");
        var conPagoRechazado = new ComprarBoletaUseCase(aforos, inventarios, boletas, estrategias, fabricas,
                rechaza, generadorQr, publicador, Clock.systemUTC(), 90);

        assertThrows(PagoRechazadoException.class,
                () -> transaccion.execute(estado -> conPagoRechazado.comprar(compraGeneral(eventoId))));

        var inventario = inventarios.buscar(eventoId, GENERAL).orElseThrow();
        assertEquals(1, inventario.disponibles(), "el descuento se deshizo");
        assertEquals(0, inventario.vendidas());
        assertEquals(0, aforos.buscar(eventoId).orElseThrow().vendidas());
        assertEquals(0, boletasJpa.countByEventoId(eventoId));
    }
}