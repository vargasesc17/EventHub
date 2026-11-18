package com.eventhub.notificaciones.infrastructure.persistencia;

import com.eventhub.notificaciones.application.AlertaAforoRepository;
import com.eventhub.notificaciones.application.CompradorRepository;
import com.eventhub.notificaciones.application.EventoRepository;
import com.eventhub.notificaciones.application.MensajeProcesadoRepository;
import com.eventhub.notificaciones.application.RecordatorioRepository;
import com.eventhub.notificaciones.domain.CompradorEvento;
import com.eventhub.notificaciones.domain.EventoLocal;
import com.eventhub.notificaciones.domain.InfoEvento;
import com.eventhub.notificaciones.domain.Recordatorio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/** Prueba los adaptadores JPA contra una base H2 en memoria (perfil test). */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RepositoriosJpaTest {

    @Autowired private EventoRepository eventos;
    @Autowired private CompradorRepository compradores;
    @Autowired private MensajeProcesadoRepository mensajes;
    @Autowired private AlertaAforoRepository alertas;
    @Autowired private RecordatorioRepository recordatorios;

    private final UUID eventoId = UUID.randomUUID();
    private final Instant inicio = Instant.parse("2030-06-01T20:00:00Z");
    private final Instant ahora = Instant.parse("2030-05-31T21:00:00Z");   // faltan 23 horas

    private InfoEvento concierto(Instant fecha) {
        return new InfoEvento(eventoId, "Concierto", fecha, "Coliseo, Chía");
    }

    private CompradorEvento ana() {
        return new CompradorEvento(UUID.randomUUID(), eventoId, UUID.randomUUID(), "Ana", "ana@correo.test",
                Instant.parse("2030-05-01T12:00:00Z"));
    }

    @Test
    void guardaYLeeElEventoLocal() {
        eventos.guardar(new EventoLocal(concierto(inicio), false, Instant.parse("2030-05-01T00:00:00Z")));

        var leido = eventos.buscar(eventoId).orElseThrow();
        assertEquals(concierto(inicio), leido.info());
        assertEquals(Instant.parse("2030-05-01T00:00:00Z"), leido.ultimaFechaEmision());
    }

    @Test
    void unCompradorNoSeGuardaDosVeces() {
        var ana = ana();
        compradores.guardarSiNoExiste(ana);
        compradores.guardarSiNoExiste(ana);

        assertEquals(1, compradores.buscarPorEvento(eventoId).size());
    }

    @Test
    void recuerdaLosMensajesProcesados() {
        var messageId = UUID.randomUUID();
        assertFalse(mensajes.yaProcesado(messageId));

        mensajes.registrar(messageId, "boleta.comprada", ahora);

        assertTrue(mensajes.yaProcesado(messageId));
    }

    @Test
    void laAlertaDeAforoSoloSeRegistraUnaVez() {
        assertTrue(alertas.registrarSiEsPrimera(eventoId, ahora));
        assertFalse(alertas.registrarSiEsPrimera(eventoId, ahora));
    }

    @Test
    void elRecordatorioPendienteSaleHastaQueSeMarcaEnviado() {
        eventos.guardar(EventoLocal.desdeCompra(concierto(inicio)));
        var ana = ana();
        compradores.guardarSiNoExiste(ana);

        List<Recordatorio> pendientes = recordatorios.pendientes(ahora, ahora.plusSeconds(24 * 3600));
        assertEquals(1, pendientes.size());
        assertEquals(ana.boletaId(), pendientes.get(0).boletaId());

        recordatorios.marcarEnviado(ana.boletaId(), inicio, ahora);

        assertTrue(recordatorios.pendientes(ahora, ahora.plusSeconds(24 * 3600)).isEmpty());
    }

    @Test
    void unEventoCanceladoNoTieneRecordatorios() {
        eventos.guardar(new EventoLocal(concierto(inicio), true, ahora));
        compradores.guardarSiNoExiste(ana());

        assertTrue(recordatorios.pendientes(ahora, ahora.plusSeconds(24 * 3600)).isEmpty());
    }
}
