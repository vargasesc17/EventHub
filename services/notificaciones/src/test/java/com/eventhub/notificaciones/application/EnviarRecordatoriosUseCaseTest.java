package com.eventhub.notificaciones.application;

import com.eventhub.notificaciones.domain.CompradorEvento;
import com.eventhub.notificaciones.domain.EventoLocal;
import com.eventhub.notificaciones.domain.InfoEvento;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EnviarRecordatoriosUseCaseTest {

    private final PuertosEnMemoria.Eventos eventos = new PuertosEnMemoria.Eventos();
    private final PuertosEnMemoria.Compradores compradores = new PuertosEnMemoria.Compradores();
    private final PuertosEnMemoria.Recordatorios recordatorios = new PuertosEnMemoria.Recordatorios(eventos, compradores);
    private final PuertosEnMemoria.Correos correos = new PuertosEnMemoria.Correos();

    private final UUID eventoId = UUID.randomUUID();
    private final Instant inicio = Instant.parse("2026-11-28T01:00:00Z");

    private EnviarRecordatoriosUseCase casoDeUsoA(String ahora) {
        return new EnviarRecordatoriosUseCase(recordatorios, correos,
                Clock.fixed(Instant.parse(ahora), ZoneOffset.UTC));
    }

    private void eventoConAna(Instant fecha, boolean cancelado) {
        eventos.guardar(new EventoLocal(new InfoEvento(eventoId, "Concierto", fecha, "Auditorio, Chía"),
                cancelado, null));
        compradores.guardarSiNoExiste(new CompradorEvento(UUID.randomUUID(), eventoId, UUID.randomUUID(),
                "Ana", "ana@correo.test", Instant.parse("2026-11-15T19:30:00Z")));
    }

    @Test
    void faltandoMenosDe24HorasEnviaElRecordatorio() {
        eventoConAna(inicio, false);

        int enviados = casoDeUsoA("2026-11-27T02:00:00Z").ejecutar();   // faltan 23 horas

        assertEquals(1, enviados);
        assertEquals(List.of("ana@correo.test"), correos.destinatarios());
        assertEquals("Mañana es Concierto", correos.enviados.get(0).asunto());
    }

    @Test
    void faltandoMasDe24HorasTodaviaNoEnvia() {
        eventoConAna(inicio, false);

        assertEquals(0, casoDeUsoA("2026-11-26T00:00:00Z").ejecutar());   // faltan 49 horas
    }

    @Test
    void noLoEnviaDosVeces() {
        eventoConAna(inicio, false);

        casoDeUsoA("2026-11-27T02:00:00Z").ejecutar();
        int segundaVez = casoDeUsoA("2026-11-27T02:01:00Z").ejecutar();

        assertEquals(0, segundaVez);
        assertEquals(1, correos.enviados.size());
    }

    @Test
    void siElEventoSeCancelaNoEnvia() {
        eventoConAna(inicio, true);

        assertEquals(0, casoDeUsoA("2026-11-27T02:00:00Z").ejecutar());
    }

    @Test
    void siCambiaLaFechaSeEnviaUnoNuevoParaLaFechaNueva() {
        eventoConAna(inicio, false);
        casoDeUsoA("2026-11-27T02:00:00Z").ejecutar();

        // El evento se pospone dos días: la nueva fecha genera su propio recordatorio.
        var nueva = Instant.parse("2026-11-30T01:00:00Z");
        eventos.guardar(new EventoLocal(new InfoEvento(eventoId, "Concierto", nueva, "Auditorio, Chía"), false,
                Instant.parse("2026-11-27T05:00:00Z")));

        assertEquals(1, casoDeUsoA("2026-11-29T02:00:00Z").ejecutar());
        assertEquals(2, correos.enviados.size());
    }

    @Test
    void siUnCorreoFallaLoReintentaEnLaSiguienteEjecucion() {
        eventoConAna(inicio, false);
        correos.fallar = true;
        assertEquals(0, casoDeUsoA("2026-11-27T02:00:00Z").ejecutar());

        correos.fallar = false;
        assertEquals(1, casoDeUsoA("2026-11-27T02:01:00Z").ejecutar());
    }
}
