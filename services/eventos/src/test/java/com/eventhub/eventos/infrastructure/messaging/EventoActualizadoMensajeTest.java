package com.eventhub.eventos.infrastructure.messaging;

import com.eventhub.eventos.domain.CategoriaBoleta;
import com.eventhub.eventos.domain.EstadoEvento;
import com.eventhub.eventos.domain.Evento;
import com.eventhub.eventos.domain.TipoBoleta;
import com.eventhub.eventos.domain.TipoCambio;
import com.eventhub.eventos.domain.ValoresAnteriores;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/** Comprueba que el mensaje cumple docs/contratos/evento.actualizado.schema.json. */
class EventoActualizadoMensajeTest {

    private final UUID eventoId = UUID.randomUUID();
    private final UUID organizadorId = UUID.randomUUID();
    private final Instant fecha = Instant.parse("2030-06-01T20:00:00Z");
    private final Instant fechaAnterior = Instant.parse("2030-05-30T20:00:00Z");

    private Evento evento(EstadoEvento estado) {
        return Evento.reconstruir(eventoId, "Concierto", "Coliseo, Chía", fecha, 500,
                organizadorId, "org@correo.com", estado,
                List.of(new CategoriaBoleta(TipoBoleta.GENERAL, 400, 80_000),
                        new CategoriaBoleta(TipoBoleta.VIP, 100, 250_000)));
    }

    @Test
    void elSobreTieneLosCamposComunes() {
        var mensaje = EventoActualizadoMensaje.desde(evento(EstadoEvento.PUBLICADO), Set.of(TipoCambio.PUBLICADO), null);

        assertNotNull(mensaje.messageId());
        assertEquals("evento.actualizado", mensaje.tipo());
        assertEquals(1, mensaje.version());
        assertEquals("eventos", mensaje.origen());
        assertNotNull(mensaje.fechaEmision());
    }

    @Test
    void cadaMensajeTieneUnMessageIdDistinto() {
        var a = EventoActualizadoMensaje.desde(evento(EstadoEvento.PUBLICADO), Set.of(TipoCambio.PUBLICADO), null);
        var b = EventoActualizadoMensaje.desde(evento(EstadoEvento.PUBLICADO), Set.of(TipoCambio.PUBLICADO), null);

        assertNotEquals(a.messageId(), b.messageId());
    }

    @Test
    void publicadoLlevaElEstadoCompletoDelEventoYSinValoresAnteriores() {
        var datos = EventoActualizadoMensaje.desde(evento(EstadoEvento.PUBLICADO), Set.of(TipoCambio.PUBLICADO), null).datos();

        assertEquals(eventoId, datos.eventoId());
        assertEquals(List.of(TipoCambio.PUBLICADO), datos.cambios());
        assertEquals(EstadoEvento.PUBLICADO, datos.estado());
        assertEquals("Concierto", datos.nombre());
        assertEquals(fecha, datos.fechaInicio());
        assertEquals("Coliseo, Chía", datos.lugar());
        assertNull(datos.valoresAnteriores());
        assertEquals(500, datos.aforoMaximo());
        assertEquals(organizadorId, datos.organizador().organizadorId());
        assertEquals("org@correo.com", datos.organizador().correo());
        assertEquals(List.of(new EventoActualizadoMensaje.Categoria(TipoBoleta.GENERAL, 400, 80_000),
                new EventoActualizadoMensaje.Categoria(TipoBoleta.VIP, 100, 250_000)), datos.categorias());
    }

    @Test
    void fechaYLugarLlevanLosDosValoresAnteriores() {
        var datos = EventoActualizadoMensaje.desde(evento(EstadoEvento.PUBLICADO),
                Set.of(TipoCambio.LUGAR, TipoCambio.FECHA),
                new ValoresAnteriores(fechaAnterior, "Auditorio principal")).datos();

        assertEquals(List.of(TipoCambio.FECHA, TipoCambio.LUGAR), datos.cambios());
        assertEquals(Map.of("fechaInicio", fechaAnterior, "lugar", "Auditorio principal"), datos.valoresAnteriores());
    }

    @Test
    void soloLugarNoIncluyeLaFechaAnterior() {
        var datos = EventoActualizadoMensaje.desde(evento(EstadoEvento.PUBLICADO), Set.of(TipoCambio.LUGAR),
                new ValoresAnteriores(null, "Auditorio principal")).datos();

        assertEquals(Map.of("lugar", "Auditorio principal"), datos.valoresAnteriores());
    }

    @Test
    void canceladoVaConEstadoCanceladoYSinValoresAnteriores() {
        var datos = EventoActualizadoMensaje.desde(evento(EstadoEvento.CANCELADO), Set.of(TipoCambio.CANCELADO), null).datos();

        assertEquals(List.of(TipoCambio.CANCELADO), datos.cambios());
        assertEquals(EstadoEvento.CANCELADO, datos.estado());
        assertNull(datos.valoresAnteriores());
    }
}