package com.eventhub.notificaciones.interfaces;

import com.eventhub.notificaciones.interfaces.mensajes.LectorMensajes;
import com.eventhub.notificaciones.interfaces.mensajes.MensajeInvalidoException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/** Comprueba que el lector entiende el ejemplo de docs/contratos/ejemplos/boleta.comprada.json. */
class LectorMensajesTest {

    private final LectorMensajes lector = new LectorMensajes();

    static final String EJEMPLO = """
            {
              "messageId": "7f3c2a10-5b1e-4c8e-9a2f-1d6b8e4c0a11",
              "tipo": "boleta.comprada",
              "version": 1,
              "fechaEmision": "2026-11-15T19:30:02Z",
              "origen": "boleteria",
              "datos": {
                "boletaId": "5d8e1f20-3a4b-4c6d-8e9f-0a1b2c3d4e5f",
                "eventoId": "3c1f9d2e-8a4b-4f6c-9e1d-2b7a5c8e0f13",
                "categoria": "VIP",
                "precioPagado": 250000,
                "fechaCompra": "2026-11-15T19:30:00Z",
                "codigoQr": "5d8e1f20-3a4b-4c6d-8e9f-0a1b2c3d4e5f.Qm9sZXRhRmlybWFkYUNvbkhNQUM",
                "comprador": {
                  "compradorId": "e4b7a1c2-9d3e-4f5a-8b6c-7d8e9f0a1b2c",
                  "nombre": "Ana Gómez",
                  "correo": "ana.gomez@correo.test"
                },
                "evento": {
                  "nombre": "Concierto de fin de semestre",
                  "fechaInicio": "2026-11-28T01:00:00Z",
                  "lugar": "Auditorio principal, Chía"
                }
              }
            }
            """;

    @Test
    void leeElEjemploDelContrato() {
        var comando = lector.leerBoletaComprada(EJEMPLO);

        assertEquals(UUID.fromString("7f3c2a10-5b1e-4c8e-9a2f-1d6b8e4c0a11"), comando.messageId());
        assertEquals(UUID.fromString("5d8e1f20-3a4b-4c6d-8e9f-0a1b2c3d4e5f"), comando.boletaId());
        assertEquals("VIP", comando.categoria());
        assertEquals("Ana Gómez", comando.nombreComprador());
        assertEquals("ana.gomez@correo.test", comando.correoComprador());
        assertEquals("Concierto de fin de semestre", comando.evento().nombre());
        assertEquals(Instant.parse("2026-11-28T01:00:00Z"), comando.evento().fechaInicio());
        assertEquals("Auditorio principal, Chía", comando.evento().lugar());
    }

    @Test
    void ignoraCamposQueNoConoce() {
        var conCampoNuevo = EJEMPLO.replace("\"version\": 1,", "\"version\": 1, \"campoNuevo\": 123,");

        assertDoesNotThrow(() -> lector.leerBoletaComprada(conCampoNuevo));
    }

    @Test
    void sinCorreoDelCompradorEsInvalido() {
        var sinCorreo = EJEMPLO.replace("\"correo\": \"ana.gomez@correo.test\"", "\"otro\": \"x\"");

        var error = assertThrows(MensajeInvalidoException.class, () -> lector.leerBoletaComprada(sinCorreo));
        assertTrue(error.getMessage().contains("datos.comprador.correo"));
    }

    @Test
    void unTextoQueNoEsJsonEsInvalido() {
        assertThrows(MensajeInvalidoException.class, () -> lector.leerBoletaComprada("Ana compró una boleta VIP"));
    }

    @Test
    void otroTipoDeMensajeEsInvalido() {
        var otroTipo = EJEMPLO.replace("\"tipo\": \"boleta.comprada\"", "\"tipo\": \"evento.actualizado\"");

        assertThrows(MensajeInvalidoException.class, () -> lector.leerBoletaComprada(otroTipo));
    }
}
