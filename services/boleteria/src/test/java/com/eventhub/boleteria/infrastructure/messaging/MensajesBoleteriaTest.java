package com.eventhub.boleteria.infrastructure.messaging;

import com.eventhub.boleteria.domain.AforoEvento;
import com.eventhub.boleteria.domain.BoletaVIP;
import com.eventhub.boleteria.domain.Compra;
import com.eventhub.boleteria.domain.EstadoBoleta;
import com.eventhub.boleteria.domain.TipoBoleta;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/** Comprueba que los mensajes tienen los campos que exigen docs/contratos y el lector de Notificaciones. */
class MensajesBoleteriaTest {

    private final UUID eventoId = UUID.randomUUID();
    private final UUID organizadorId = UUID.randomUUID();
    private final Instant fechaEvento = Instant.parse("2030-06-01T20:00:00Z");

    private AforoEvento evento(int aforoMaximo, int vendidas) {
        return new AforoEvento(eventoId, "Concierto", fechaEvento, "Movistar Arena", organizadorId,
                "org@correo.com", aforoMaximo, vendidas, false, false);
    }

    @Test
    void boletaCompradaLlevaBoletaCompradorEventoYQr() {
        var compradorId = UUID.randomUUID();
        var boleta = new BoletaVIP(UUID.randomUUID(), eventoId, compradorId, 150_000, EstadoBoleta.ACTIVA,
                Instant.parse("2030-01-01T10:00:00.123Z"), List.of("Zona VIP"));
        var compra = new Compra(eventoId, TipoBoleta.VIP, compradorId, "Ana Pérez", "ana@correo.com", null, null);

        var mensaje = BoletaCompradaMensaje.desde(boleta, compra, evento(1000, 1), "qr-firmado");

        assertNotNull(mensaje.messageId());
        assertEquals("boleta.comprada", mensaje.tipo());
        assertEquals("boleteria", mensaje.origen());
        var d = mensaje.datos();
        assertEquals(boleta.getId(), d.boletaId());
        assertEquals(eventoId, d.eventoId());
        assertEquals(TipoBoleta.VIP, d.categoria());
        assertEquals(150_000, d.precioPagado());
        assertEquals(Instant.parse("2030-01-01T10:00:00Z"), d.fechaCompra());
        assertEquals("qr-firmado", d.codigoQr());
        assertEquals(new BoletaCompradaMensaje.Comprador(compradorId, "Ana Pérez", "ana@correo.com"), d.comprador());
        assertEquals(new BoletaCompradaMensaje.Evento("Concierto", fechaEvento, "Movistar Arena"), d.evento());
    }

    @Test
    void aforoCriticoLlevaOrganizadorVendidasYUmbral() {
        var mensaje = AforoCriticoMensaje.desde(evento(500, 451), 90);

        assertEquals("evento.aforo.critico", mensaje.tipo());
        var d = mensaje.datos();
        assertEquals(eventoId, d.eventoId());
        assertEquals("Concierto", d.nombreEvento());
        assertEquals(new AforoCriticoMensaje.Organizador(organizadorId, "org@correo.com"), d.organizador());
        assertEquals(500, d.aforoMaximo());
        assertEquals(451, d.vendidas());
        assertEquals(90, d.umbral());
    }

    @Test
    void elPorcentajeVaConUnDecimalComoEnElEjemploDelContrato() {
        assertEquals(90.2, AforoCriticoMensaje.desde(evento(500, 451), 90).datos().porcentajeVendido());
        assertEquals(90.0, AforoCriticoMensaje.desde(evento(1000, 900), 90).datos().porcentajeVendido());
        assertEquals(66.7, AforoCriticoMensaje.desde(evento(3, 2), 90).datos().porcentajeVendido());
    }
}