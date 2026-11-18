package com.eventhub.notificaciones.application;

import com.eventhub.notificaciones.domain.CompradorEvento;
import com.eventhub.notificaciones.domain.EventoLocal;
import com.eventhub.notificaciones.domain.InfoEvento;
import com.eventhub.notificaciones.domain.TipoCambio;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ProcesarEventoActualizadoUseCaseTest {

    private final PuertosEnMemoria.Mensajes mensajes = new PuertosEnMemoria.Mensajes();
    private final PuertosEnMemoria.Eventos eventos = new PuertosEnMemoria.Eventos();
    private final PuertosEnMemoria.Compradores compradores = new PuertosEnMemoria.Compradores();
    private final PuertosEnMemoria.Correos correos = new PuertosEnMemoria.Correos();
    private final Clock reloj = Clock.fixed(Instant.parse("2026-11-20T16:45:10Z"), ZoneOffset.UTC);

    private final ProcesarEventoActualizadoUseCase casoDeUso =
            new ProcesarEventoActualizadoUseCase(mensajes, eventos, compradores, correos, reloj);

    private final UUID eventoId = UUID.randomUUID();
    private final Instant fechaOriginal = Instant.parse("2026-11-28T01:00:00Z");
    private final Instant fechaNueva = Instant.parse("2026-11-30T01:00:00Z");
    private final InfoEvento original = new InfoEvento(eventoId, "Concierto", fechaOriginal, "Auditorio, Chía");

    /** Ana y Pedro compraron; Ana compró dos boletas. */
    private void conCompradores() {
        eventos.guardar(EventoLocal.desdeCompra(original));
        compradores.guardarSiNoExiste(comprador("Ana", "ana@correo.test"));
        compradores.guardarSiNoExiste(comprador("Ana", "ANA@correo.test"));
        compradores.guardarSiNoExiste(comprador("Pedro", "pedro@correo.test"));
    }

    private CompradorEvento comprador(String nombre, String correo) {
        return new CompradorEvento(UUID.randomUUID(), eventoId, UUID.randomUUID(), nombre, correo, reloj.instant());
    }

    private EventoActualizadoComando cambio(Instant fechaEmision, Set<TipoCambio> cambios, InfoEvento evento,
                                            Instant fechaAnterior, String lugarAnterior) {
        return new EventoActualizadoComando(UUID.randomUUID(), fechaEmision, cambios, evento,
                fechaAnterior, lugarAnterior);
    }

    private EventoActualizadoComando cambioDeFecha(Instant fechaEmision) {
        return cambio(fechaEmision, Set.of(TipoCambio.FECHA),
                new InfoEvento(eventoId, "Concierto", fechaNueva, "Auditorio, Chía"), fechaOriginal, null);
    }

    @Test
    void unCambioDeFechaAvisaUnaVezACadaComprador() {
        conCompradores();

        var resultado = casoDeUso.ejecutar(cambioDeFecha(Instant.parse("2026-11-20T16:45:00Z")));

        assertEquals(Resultado.PROCESADO, resultado);
        // Ana tiene dos boletas pero recibe UN correo (RF-04: "una sola vez").
        assertEquals(List.of("ana@correo.test", "pedro@correo.test"), correos.destinatarios());
        assertTrue(correos.enviados.get(0).asunto().startsWith("Cambio en tu evento"));
        assertEquals(fechaNueva, eventos.buscar(eventoId).orElseThrow().info().fechaInicio());
    }

    @Test
    void unaCancelacionAvisaYMarcaElEventoComoCancelado() {
        conCompradores();

        casoDeUso.ejecutar(cambio(Instant.parse("2026-11-20T16:45:00Z"), Set.of(TipoCambio.CANCELADO),
                original, null, null));

        assertEquals(2, correos.enviados.size());
        assertTrue(correos.enviados.get(0).asunto().startsWith("Evento cancelado"));
        assertTrue(eventos.buscar(eventoId).orElseThrow().cancelado());
    }

    @Test
    void publicadoEInventarioNoEnvianCorreos() {
        conCompradores();

        var publicado = casoDeUso.ejecutar(cambio(Instant.parse("2026-11-20T10:00:00Z"),
                Set.of(TipoCambio.PUBLICADO), original, null, null));
        var inventario = casoDeUso.ejecutar(cambio(Instant.parse("2026-11-20T11:00:00Z"),
                Set.of(TipoCambio.INVENTARIO), original, null, null));

        assertEquals(Resultado.SIN_ACCION, publicado);
        assertEquals(Resultado.SIN_ACCION, inventario);
        assertTrue(correos.enviados.isEmpty());
    }

    @Test
    void unMensajeRepetidoNoAvisaDosVeces() {
        conCompradores();
        var comando = cambioDeFecha(Instant.parse("2026-11-20T16:45:00Z"));

        casoDeUso.ejecutar(comando);
        var segundaVez = casoDeUso.ejecutar(comando);

        assertEquals(Resultado.DUPLICADO, segundaVez);
        assertEquals(2, correos.enviados.size());
    }

    @Test
    void unMensajeViejoQueLlegaTardeSeIgnora() {
        conCompradores();
        casoDeUso.ejecutar(cambioDeFecha(Instant.parse("2026-11-20T16:45:00Z")));
        correos.enviados.clear();

        // Un cambio de lugar emitido ANTES que el de fecha, pero que llegó después.
        var viejo = cambio(Instant.parse("2026-11-20T09:00:00Z"), Set.of(TipoCambio.LUGAR),
                new InfoEvento(eventoId, "Concierto", fechaOriginal, "Coliseo, Chía"), null, "Auditorio, Chía");

        assertEquals(Resultado.DESACTUALIZADO, casoDeUso.ejecutar(viejo));
        assertTrue(correos.enviados.isEmpty());
        assertEquals(fechaNueva, eventos.buscar(eventoId).orElseThrow().info().fechaInicio());
    }

    @Test
    void elCorreoDeCambioMuestraElAntesYElDespues() {
        conCompradores();

        casoDeUso.ejecutar(cambio(Instant.parse("2026-11-20T16:45:00Z"), Set.of(TipoCambio.LUGAR),
                new InfoEvento(eventoId, "Concierto", fechaOriginal, "Coliseo, Chía"), null, "Auditorio, Chía"));

        String cuerpo = correos.enviados.get(0).cuerpoHtml();
        assertTrue(cuerpo.contains("antes en Auditorio, Chía"));
        assertTrue(cuerpo.contains("ahora en <b>Coliseo, Chía</b>"));
    }
}
