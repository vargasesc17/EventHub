package com.eventhub.boleteria.infrastructure.persistencia;

import com.eventhub.boleteria.application.AforoRepository;
import com.eventhub.boleteria.application.BoletaRepository;
import com.eventhub.boleteria.application.InventarioRepository;
import com.eventhub.boleteria.application.MensajeProcesadoRepository;
import com.eventhub.boleteria.domain.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RepositoriosJpaTest {

    @Autowired private AforoRepository aforos;
    @Autowired private InventarioRepository inventarios;
    @Autowired private MensajeProcesadoRepository mensajes;
    @Autowired private EntityManager entityManager;
    @Autowired private BoletaRepository boletas;
    @Autowired private BoletaJpaRepository boletasJpa;

    private final UUID eventoId = UUID.randomUUID();
    private final UUID organizadorId = UUID.randomUUID();
    private final Instant fecha = Instant.parse("2030-06-01T20:00:00Z");

    /** Un evento con ventas y alerta ya enviada, para comprobar que los cambios puntuales no los tocan. */
    private AforoEvento conVentas() {
        return new AforoEvento(eventoId, "Concierto", fecha, "Movistar Arena", organizadorId, "org@correo.com",
                1000, 950, true, false);
    }

    private void sincronizar() {
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void guardaYLeeLaCopiaDelEvento() {
        aforos.crear(conVentas());
        sincronizar();

        assertEquals(conVentas(), aforos.buscar(eventoId).orElseThrow());
    }

    @Test
    void guardaUnaBoletaConLosCamposDeSuTipo() {
        var boleta = new BoletaVIP(UUID.randomUUID(), eventoId, UUID.randomUUID(), 150_000, EstadoBoleta.ACTIVA,
                fecha, List.of("Zona VIP", "Ingreso preferencial"));

        boletas.guardar(boleta);
        sincronizar();

        var fila = boletasJpa.findById(boleta.getId()).orElseThrow();
        assertEquals(TipoBoleta.VIP, fila.getCategoria());
        assertEquals(150_000, fila.getPrecioPagado());
        assertEquals(EstadoBoleta.ACTIVA, fila.getEstado());
        assertEquals("Zona VIP|Ingreso preferencial", fila.getBeneficios());
        assertNull(fila.getCarnetEstudiantil());
        assertNull(fila.getEmitidaPor());
    }

    @Test
    void existeDistingueLosEventosConocidos() {
        aforos.crear(conVentas());
        sincronizar();

        assertTrue(aforos.existe(eventoId));
        assertFalse(aforos.existe(UUID.randomUUID()));
    }

    @Test
    void actualizarProgramacionSoloCambiaFechaYLugar() {
        aforos.crear(conVentas());
        sincronizar();
        var nuevaFecha = Instant.parse("2030-06-03T20:00:00Z");

        aforos.actualizarProgramacion(eventoId, nuevaFecha, "Coliseo, Chía");

        var leido = aforos.buscar(eventoId).orElseThrow();
        assertEquals(nuevaFecha, leido.fechaInicio());
        assertEquals("Coliseo, Chía", leido.lugar());
        assertEquals(950, leido.vendidas());
        assertTrue(leido.alertaEnviada());
        assertTrue(leido.aceptaVentas());
    }

    @Test
    void bloquearVentasNoTocaLosContadores() {
        aforos.crear(conVentas());
        sincronizar();

        aforos.bloquearVentas(eventoId);

        var leido = aforos.buscar(eventoId).orElseThrow();
        assertFalse(leido.aceptaVentas());
        assertEquals(950, leido.vendidas());
        assertEquals("Movistar Arena", leido.lugar());
    }

    @Test
    void guardaElInventarioDeCadaCategoria() {
        inventarios.crear(List.of(
                InventarioCategoria.nuevo(eventoId, TipoBoleta.GENERAL, 800, 50_000),
                InventarioCategoria.nuevo(eventoId, TipoBoleta.VIP, 200, 150_000)));
        sincronizar();

        var leidos = inventarios.buscarPorEvento(eventoId);

        assertEquals(2, leidos.size());
        assertTrue(leidos.contains(InventarioCategoria.nuevo(eventoId, TipoBoleta.GENERAL, 800, 50_000)));
        assertTrue(leidos.contains(InventarioCategoria.nuevo(eventoId, TipoBoleta.VIP, 200, 150_000)));
        assertTrue(inventarios.buscarPorEvento(UUID.randomUUID()).isEmpty());
    }

    @Test
    void recuerdaLosMensajesProcesados() {
        var messageId = UUID.randomUUID();
        assertFalse(mensajes.yaProcesado(messageId));

        mensajes.registrar(messageId, "evento.actualizado", Instant.now());
        sincronizar();

        assertTrue(mensajes.yaProcesado(messageId));
    }

    @Test
    void cancelarPorEventoSoloTocaLasActivasDeEseEvento() {
        var activa = new BoletaGeneral(UUID.randomUUID(), eventoId, UUID.randomUUID(), 50_000, EstadoBoleta.ACTIVA, fecha);
        var usada = new BoletaGeneral(UUID.randomUUID(), eventoId, UUID.randomUUID(), 50_000, EstadoBoleta.ACTIVA, fecha);
        var deOtroEvento = new BoletaGeneral(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), 50_000, EstadoBoleta.ACTIVA, fecha);
        boletas.guardar(activa);
        boletas.guardar(usada);
        boletas.guardar(deOtroEvento);
        sincronizar();
        boletas.marcarUsadaSiActiva(usada.getId());

        int canceladas = boletas.cancelarPorEvento(eventoId);

        assertEquals(1, canceladas);
        assertEquals(EstadoBoleta.CANCELADA, boletas.buscar(activa.getId()).orElseThrow().getEstado());
        assertEquals(EstadoBoleta.USADA, boletas.buscar(usada.getId()).orElseThrow().getEstado(),
                "quien ya entró queda registrado como que entró");
        assertEquals(EstadoBoleta.ACTIVA, boletas.buscar(deOtroEvento.getId()).orElseThrow().getEstado(),
                "no toca boletas de otros eventos");
    }
}