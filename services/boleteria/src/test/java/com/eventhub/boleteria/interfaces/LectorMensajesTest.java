package com.eventhub.boleteria.interfaces;

import com.eventhub.boleteria.application.TipoCambio;
import com.eventhub.boleteria.domain.TipoBoleta;
import com.eventhub.boleteria.interfaces.mensajes.LectorMensajes;
import com.eventhub.boleteria.interfaces.mensajes.MensajeInvalidoException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class LectorMensajesTest {

    /** El ejemplo de docs/contratos/ejemplos/evento.actualizado.publicado.json. */
    private static final String PUBLICADO = """
            {
              "messageId": "1a2b3c4d-5e6f-4a7b-8c9d-0e1f2a3b4c5d",
              "tipo": "evento.actualizado",
              "version": 1,
              "fechaEmision": "2026-11-12T14:00:00Z",
              "origen": "eventos",
              "datos": {
                "eventoId": "3c1f9d2e-8a4b-4f6c-9e1d-2b7a5c8e0f13",
                "cambios": ["PUBLICADO"],
                "estado": "PUBLICADO",
                "nombre": "Concierto de fin de semestre",
                "fechaInicio": "2026-11-28T01:00:00Z",
                "lugar": "Auditorio principal, Chía",
                "valoresAnteriores": null,
                "aforoMaximo": 500,
                "organizador": {
                  "organizadorId": "a9e2c4b1-6d3f-4a8e-b2c7-1f5d9e3a7c20",
                  "correo": "organizador@eventhub.test"
                },
                "categorias": [
                  {"categoria": "GENERAL", "cupos": 400, "precioBase": 80000},
                  {"categoria": "VIP", "cupos": 50, "precioBase": 250000},
                  {"categoria": "ESTUDIANTE", "cupos": 50, "precioBase": 50000}
                ]
              }
            }
            """;

    private final LectorMensajes lector = new LectorMensajes();

    @Test
    void leeElEjemploDelContrato() {
        var comando = lector.leerEventoActualizado(PUBLICADO);

        assertEquals(UUID.fromString("1a2b3c4d-5e6f-4a7b-8c9d-0e1f2a3b4c5d"), comando.messageId());
        assertEquals(UUID.fromString("3c1f9d2e-8a4b-4f6c-9e1d-2b7a5c8e0f13"), comando.eventoId());
        assertEquals(Set.of(TipoCambio.PUBLICADO), comando.cambios());
        assertEquals("Concierto de fin de semestre", comando.nombre());
        assertEquals(Instant.parse("2026-11-28T01:00:00Z"), comando.fechaInicio());
        assertEquals("Auditorio principal, Chía", comando.lugar());
        assertEquals(500, comando.aforoMaximo());
        assertEquals("organizador@eventhub.test", comando.organizadorCorreo());
        assertEquals(3, comando.categorias().size());
        assertEquals(TipoBoleta.VIP, comando.categorias().get(1).categoria());
        assertEquals(50, comando.categorias().get(1).cupos());
        assertEquals(250_000, comando.categorias().get(1).precioBase());
    }

    @Test
    void leeVariosCambiosEnUnSoloMensaje() {
        var comando = lector.leerEventoActualizado(PUBLICADO.replace("[\"PUBLICADO\"]", "[\"FECHA\", \"LUGAR\"]"));

        assertEquals(Set.of(TipoCambio.FECHA, TipoCambio.LUGAR), comando.cambios());
    }

    @Test
    void unCambioQueNoExisteEsInvalido() {
        var texto = PUBLICADO.replace("[\"PUBLICADO\"]", "[\"REUBICADO\"]");

        assertThrows(MensajeInvalidoException.class, () -> lector.leerEventoActualizado(texto));
    }

    @Test
    void sinAforoMaximoEsInvalido() {
        var texto = PUBLICADO.replace("\"aforoMaximo\": 500,", "");

        var error = assertThrows(MensajeInvalidoException.class, () -> lector.leerEventoActualizado(texto));

        assertTrue(error.getMessage().contains("aforoMaximo"));
    }

    @Test
    void unTextoQueNoEsJsonEsInvalido() {
        assertThrows(MensajeInvalidoException.class, () -> lector.leerEventoActualizado("esto no es JSON"));
    }

    @Test
    void otroTipoDeMensajeEsInvalido() {
        var texto = PUBLICADO.replace("\"tipo\": \"evento.actualizado\"", "\"tipo\": \"boleta.comprada\"");

        assertThrows(MensajeInvalidoException.class, () -> lector.leerEventoActualizado(texto));
    }
}