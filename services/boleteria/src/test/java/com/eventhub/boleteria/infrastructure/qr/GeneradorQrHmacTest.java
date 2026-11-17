package com.eventhub.boleteria.infrastructure.qr;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GeneradorQrHmacTest {

    private final GeneradorQrHmac generador = new GeneradorQrHmac("secreto-de-prueba");
    private final UUID boletaId = UUID.fromString("5d8e1f20-3a4b-4c6d-8e9f-0a1b2c3d4e5f");

    @Test
    void elQrEsElIdDeLaBoletaPuntoLaFirma() {
        var qr = generador.generar(boletaId);

        var partes = qr.split("\\.");
        assertEquals(2, partes.length);
        assertEquals(boletaId.toString(), partes[0]);
        assertTrue(partes[1].matches("[A-Za-z0-9_-]{43}"), "HMAC-SHA256 en Base64 URL sin relleno: 43 caracteres");
    }

    @Test
    void laMismaBoletaSiempreDaElMismoQr() {
        assertEquals(generador.generar(boletaId), generador.generar(boletaId));
    }

    @Test
    void otraBoletaDaOtraFirma() {
        var otra = generador.generar(UUID.randomUUID()).split("\\.")[1];

        assertNotEquals(generador.generar(boletaId).split("\\.")[1], otra);
    }

    @Test
    void sinElMismoSecretoNoSePuedeFabricarElQr() {
        var conOtroSecreto = new GeneradorQrHmac("otro-secreto").generar(boletaId);

        assertNotEquals(generador.generar(boletaId), conOtroSecreto);
    }

    @Test
    void noArrancaSinSecreto() {
        assertThrows(IllegalStateException.class, () -> new GeneradorQrHmac(""));
        assertThrows(IllegalStateException.class, () -> new GeneradorQrHmac(null));
    }
}