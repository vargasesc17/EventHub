package com.eventhub.notificaciones.application;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ProcesarAforoCriticoUseCaseTest {

    private final PuertosEnMemoria.Mensajes mensajes = new PuertosEnMemoria.Mensajes();
    private final PuertosEnMemoria.Alertas alertas = new PuertosEnMemoria.Alertas();
    private final PuertosEnMemoria.Correos correos = new PuertosEnMemoria.Correos();
    private final Clock reloj = Clock.fixed(Instant.parse("2026-11-25T22:10:06Z"), ZoneOffset.UTC);

    private final ProcesarAforoCriticoUseCase casoDeUso =
            new ProcesarAforoCriticoUseCase(mensajes, alertas, correos, reloj);

    private final UUID eventoId = UUID.randomUUID();

    private AforoCriticoComando alerta() {
        return new AforoCriticoComando(UUID.randomUUID(), eventoId, "Concierto de fin de semestre",
                "organizador@eventhub.test", 500, 451, 90.2, 90);
    }

    @Test
    void avisaAlOrganizador() {
        assertEquals(Resultado.PROCESADO, casoDeUso.ejecutar(alerta()));

        assertEquals(List.of("organizador@eventhub.test"), correos.destinatarios());
        var correo = correos.enviados.get(0);
        assertEquals("Aforo crítico: Concierto de fin de semestre", correo.asunto());
        assertTrue(correo.cuerpoHtml().contains("451 de 500"));
    }

    @Test
    void soloAvisaUnaVezPorEventoAunqueLleguenDosAlertas() {
        casoDeUso.ejecutar(alerta());
        var segunda = casoDeUso.ejecutar(alerta());   // otro messageId, mismo evento

        assertEquals(Resultado.SIN_ACCION, segunda);
        assertEquals(1, correos.enviados.size());
    }

    @Test
    void unMensajeRepetidoEsDuplicado() {
        var comando = alerta();
        casoDeUso.ejecutar(comando);

        assertEquals(Resultado.DUPLICADO, casoDeUso.ejecutar(comando));
    }
}
