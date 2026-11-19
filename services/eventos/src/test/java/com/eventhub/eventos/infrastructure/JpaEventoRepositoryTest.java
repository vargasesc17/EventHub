package com.eventhub.eventos.infrastructure;

import com.eventhub.eventos.application.EventoRepository;
import com.eventhub.eventos.domain.CategoriaBoleta;
import com.eventhub.eventos.domain.EstadoEvento;
import com.eventhub.eventos.domain.Evento;
import com.eventhub.eventos.domain.TipoBoleta;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class JpaEventoRepositoryTest {

    @Autowired
    private EventoRepository repositorio;

    @Autowired
    private EntityManager entityManager;

    private final Instant futuro = Instant.now().plus(30, ChronoUnit.DAYS).truncatedTo(ChronoUnit.SECONDS);
    private final Instant pasado = Instant.now().minus(1, ChronoUnit.DAYS).truncatedTo(ChronoUnit.SECONDS);

    private Evento evento(String nombre, EstadoEvento estado, Instant fecha) {
        return Evento.reconstruir(UUID.randomUUID(), nombre, "Movistar Arena", fecha, 1000,
                UUID.randomUUID(), "org@correo.com", estado,
                List.of(new CategoriaBoleta(TipoBoleta.GENERAL, 800, 50_000),
                        new CategoriaBoleta(TipoBoleta.VIP, 200, 150_000)));
    }

    /** Obliga a escribir en la base y a leer de ella, no de la memoria de Hibernate. */
    private void sincronizar() {
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void guardaYRecuperaUnEventoCompleto() {
        var original = evento("Concierto", EstadoEvento.PUBLICADO, futuro);

        repositorio.guardar(original);
        sincronizar();
        var leido = repositorio.buscarPorId(original.getId()).orElseThrow();

        assertEquals(original.getId(), leido.getId());
        assertEquals("Concierto", leido.getNombre());
        assertEquals("Movistar Arena", leido.getLugar());
        assertEquals(futuro, leido.getFechaInicio());
        assertEquals(1000, leido.getAforoMaximo());
        assertEquals(original.getOrganizadorId(), leido.getOrganizadorId());
        assertEquals("org@correo.com", leido.getOrganizadorCorreo());
        assertEquals(EstadoEvento.PUBLICADO, leido.getEstado());
        assertEquals(original.getCategorias(), leido.getCategorias());
    }

    @Test
    void buscarPorIdInexistenteDevuelveVacio() {
        assertTrue(repositorio.buscarPorId(UUID.randomUUID()).isEmpty());
    }

    @Test
    void guardarUnEventoExistenteLoActualiza() {
        var evento = evento("Concierto", EstadoEvento.BORRADOR, futuro);
        repositorio.guardar(evento);
        sincronizar();

        evento.editar("Festival", "Parque Simón Bolívar", futuro, 500,
                List.of(new CategoriaBoleta(TipoBoleta.ESTUDIANTE, 500, 30_000)), Instant.now());
        repositorio.guardar(evento);
        sincronizar();

        var leido = repositorio.buscarPorId(evento.getId()).orElseThrow();
        assertEquals("Festival", leido.getNombre());
        assertEquals(500, leido.getAforoMaximo());
        assertEquals(List.of(new CategoriaBoleta(TipoBoleta.ESTUDIANTE, 500, 30_000)), leido.getCategorias());
    }

    @Test
    void catalogoSoloTraePublicadosConFechaFutura() {
        repositorio.guardar(evento("Visible", EstadoEvento.PUBLICADO, futuro));
        repositorio.guardar(evento("En borrador", EstadoEvento.BORRADOR, futuro));
        repositorio.guardar(evento("Cancelado", EstadoEvento.CANCELADO, futuro));
        repositorio.guardar(evento("Ya pasó", EstadoEvento.PUBLICADO, pasado));
        sincronizar();

        var catalogo = repositorio.buscarPublicadosDesde(Instant.now());

        assertEquals(1, catalogo.size());
        assertEquals("Visible", catalogo.get(0).getNombre());
    }
}