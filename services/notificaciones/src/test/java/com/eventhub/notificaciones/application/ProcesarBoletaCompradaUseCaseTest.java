package com.eventhub.notificaciones.application;

import com.eventhub.notificaciones.domain.CompradorEvento;
import com.eventhub.notificaciones.domain.Correo;
import com.eventhub.notificaciones.domain.EventoLocal;
import com.eventhub.notificaciones.domain.InfoEvento;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/** El caso de uso se prueba con puertos falsos en memoria: sin RabbitMQ, base de datos ni correo. */
class ProcesarBoletaCompradaUseCaseTest {

    private final MensajesEnMemoria mensajes = new MensajesEnMemoria();
    private final EventosEnMemoria eventos = new EventosEnMemoria();
    private final CompradoresEnMemoria compradores = new CompradoresEnMemoria();
    private final CorreosEnMemoria correos = new CorreosEnMemoria();
    private final GeneradorImagenQr qrFalso = contenido -> ("PNG:" + contenido).getBytes();
    private final Clock reloj = Clock.fixed(Instant.parse("2026-11-15T19:30:05Z"), ZoneOffset.UTC);

    private final ProcesarBoletaCompradaUseCase casoDeUso =
            new ProcesarBoletaCompradaUseCase(mensajes, eventos, compradores, qrFalso, correos, reloj);

    private final InfoEvento concierto = new InfoEvento(UUID.randomUUID(), "Concierto de fin de semestre",
            Instant.parse("2026-11-28T01:00:00Z"), "Auditorio principal, Chía");

    private BoletaCompradaComando compraDeAna(UUID messageId) {
        return new BoletaCompradaComando(messageId, UUID.randomUUID(), "VIP", "codigo-qr-firmado",
                UUID.randomUUID(), "Ana Gómez", "ana.gomez@correo.test", concierto);
    }

    @Test
    void enviaElCorreoDeConfirmacionConElQr() {
        var resultado = casoDeUso.ejecutar(compraDeAna(UUID.randomUUID()));

        assertEquals(Resultado.PROCESADO, resultado);
        assertEquals(1, correos.enviados.size());
        Correo correo = correos.enviados.get(0);
        assertEquals("ana.gomez@correo.test", correo.para());
        assertEquals("Tu boleta para Concierto de fin de semestre", correo.asunto());
        assertTrue(correo.cuerpoHtml().contains("Ana Gómez"));
        assertTrue(correo.cuerpoHtml().contains("VIP"));
        assertArrayEquals("PNG:codigo-qr-firmado".getBytes(), correo.qrPng());
    }

    @Test
    void guardaAlCompradorYAlEventoEnLaCopiaLocal() {
        var comando = compraDeAna(UUID.randomUUID());

        casoDeUso.ejecutar(comando);

        List<CompradorEvento> delEvento = compradores.buscarPorEvento(concierto.eventoId());
        assertEquals(1, delEvento.size());
        assertEquals(comando.boletaId(), delEvento.get(0).boletaId());
        assertEquals(reloj.instant(), delEvento.get(0).registradoEn());
        assertEquals(concierto, eventos.buscar(concierto.eventoId()).orElseThrow().info());
    }

    @Test
    void unMensajeRepetidoNoEnviaOtroCorreo() {
        var comando = compraDeAna(UUID.randomUUID());

        casoDeUso.ejecutar(comando);
        var segundaVez = casoDeUso.ejecutar(comando);

        assertEquals(Resultado.DUPLICADO, segundaVez);
        assertEquals(1, correos.enviados.size());
    }

    @Test
    void noPisaLosDatosDeUnEventoQueYaConocia() {
        var actualizado = new InfoEvento(concierto.eventoId(), concierto.nombre(),
                Instant.parse("2026-11-30T01:00:00Z"), "Coliseo, Chía");
        eventos.guardar(new EventoLocal(actualizado, false, Instant.parse("2026-11-15T10:00:00Z")));

        casoDeUso.ejecutar(compraDeAna(UUID.randomUUID()));

        assertEquals("Coliseo, Chía", eventos.buscar(concierto.eventoId()).orElseThrow().info().lugar());
    }

    @Test
    void siElCorreoFallaNoMarcaElMensajeComoProcesado() {
        var comando = compraDeAna(UUID.randomUUID());
        correos.fallar = true;

        assertThrows(IllegalStateException.class, () -> casoDeUso.ejecutar(comando));

        assertFalse(mensajes.yaProcesado(comando.messageId()));
    }

    // ---- Puertos falsos en memoria ----

    static class MensajesEnMemoria implements MensajeProcesadoRepository {
        final Set<UUID> procesados = new HashSet<>();
        public boolean yaProcesado(UUID messageId) { return procesados.contains(messageId); }
        public void registrar(UUID messageId, String tipo, Instant procesadoEn) { procesados.add(messageId); }
    }

    static class EventosEnMemoria implements EventoRepository {
        final Map<UUID, EventoLocal> eventos = new HashMap<>();
        public Optional<EventoLocal> buscar(UUID eventoId) { return Optional.ofNullable(eventos.get(eventoId)); }
        public void guardar(EventoLocal evento) { eventos.put(evento.info().eventoId(), evento); }
    }

    static class CompradoresEnMemoria implements CompradorRepository {
        final Map<UUID, CompradorEvento> porBoleta = new HashMap<>();
        public void guardarSiNoExiste(CompradorEvento c) { porBoleta.putIfAbsent(c.boletaId(), c); }
        public List<CompradorEvento> buscarPorEvento(UUID eventoId) {
            return porBoleta.values().stream().filter(c -> c.eventoId().equals(eventoId)).toList();
        }
    }

    static class CorreosEnMemoria implements EnviadorCorreo {
        final List<Correo> enviados = new ArrayList<>();
        boolean fallar;
        public void enviar(Correo correo) {
            if (fallar) {
                throw new IllegalStateException("El servidor de correo no responde");
            }
            enviados.add(correo);
        }
    }
}
