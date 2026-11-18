package com.eventhub.boleteria.infrastructure.qr;

import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/** #24 · Primer paso del check-in (ADR-0004): solo un QR emitido con nuestra clave es auténtico. */
class ValidacionQrHmacTest {

    private final GeneradorQrHmac qr = new GeneradorQrHmac("secreto-de-prueba");
    private final UUID boletaId = UUID.fromString("5d8e1f20-3a4b-4c6d-8e9f-0a1b2c3d4e5f");

    @Test
    void unQrQueEmitimosEsAutentico() {
        assertEquals(Optional.of(boletaId), qr.boletaIdSiEsAutentico(qr.generar(boletaId)));
    }

    @Test
    void siSeCambiaUnaLetraDeLaFirmaSeRechaza() {
        var original = qr.generar(boletaId);
        var ultima = original.charAt(original.length() - 1);
        var alterado = original.substring(0, original.length() - 1) + (ultima == 'A' ? 'B' : 'A');

        assertTrue(qr.boletaIdSiEsAutentico(alterado).isEmpty());
    }

    @Test
    void ponerLaFirmaDeUnaBoletaEnOtraSeRechaza() {
        var firmaDeOtra = qr.generar(UUID.randomUUID()).split("\\.")[1];

        assertTrue(qr.boletaIdSiEsAutentico(boletaId + "." + firmaDeOtra).isEmpty());
    }

    @Test
    void unQrFirmadoConOtraClaveSeRechaza() {
        var deOtroSistema = new GeneradorQrHmac("otra-clave").generar(boletaId);

        assertTrue(qr.boletaIdSiEsAutentico(deOtroSistema).isEmpty());
    }

    @Test
    void textosQueNoTienenLaFormaDeUnQrSeRechazan() {
        assertTrue(qr.boletaIdSiEsAutentico(null).isEmpty());
        assertTrue(qr.boletaIdSiEsAutentico("").isEmpty());
        assertTrue(qr.boletaIdSiEsAutentico("hola").isEmpty());
        assertTrue(qr.boletaIdSiEsAutentico(boletaId.toString()).isEmpty());
        assertTrue(qr.boletaIdSiEsAutentico("no-es-uuid.firma").isEmpty());
        assertTrue(qr.boletaIdSiEsAutentico(qr.generar(boletaId) + ".extra").isEmpty());
    }

    @Test
    void elIdEnMayusculasNoEsElQueEmitimos() {
        var original = qr.generar(boletaId);
        var partes = original.split("\\.");

        assertTrue(qr.boletaIdSiEsAutentico(partes[0].toUpperCase() + "." + partes[1]).isEmpty());
    }
}
