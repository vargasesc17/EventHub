package com.eventhub.boleteria.infrastructure.qr;

import com.eventhub.boleteria.application.GeneradorQr;
import com.eventhub.boleteria.application.ValidadorQr;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

/**
 * ADR-0004: el QR es "boletaId.firma", con firma = HMAC-SHA256(secreto, boletaId).
 * Sin el secreto nadie puede fabricar un QR válido; con él, se verifica sin consultar la base.
 * La misma clase genera (compra, #23) y verifica (check-in, #24), con la misma clave.
 */
@Component
public class GeneradorQrHmac implements GeneradorQr, ValidadorQr {

    private static final String ALGORITMO = "HmacSHA256";

    private final SecretKeySpec clave;

    public GeneradorQrHmac(@Value("${boleteria.qr.secreto}") String secreto) {
        if (secreto == null || secreto.isBlank()) {
            throw new IllegalStateException("Falta la variable BOLETERIA_QR_SECRETO (ADR-0004)");
        }
        this.clave = new SecretKeySpec(secreto.getBytes(StandardCharsets.UTF_8), ALGORITMO);
    }

    @Override
    public String generar(UUID boletaId) {
        return boletaId + "." + firmar(boletaId.toString());
    }

    /**
     * Paso 1 del check-in (ADR-0004): recalcula la firma y la compara. No consulta la base.
     * Cualquier cosa que no tenga la forma "uuid.firma" con la firma correcta se rechaza.
     */
    @Override
    public Optional<UUID> boletaIdSiEsAutentico(String codigoQr) {
        if (codigoQr == null) {
            return Optional.empty();
        }
        int punto = codigoQr.indexOf('.');
        if (punto <= 0 || punto != codigoQr.lastIndexOf('.')) {
            return Optional.empty();
        }
        String id = codigoQr.substring(0, punto);
        String firma = codigoQr.substring(punto + 1);
        UUID boletaId;
        try {
            boletaId = UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
        // El id debe estar escrito exactamente como lo generamos (minúsculas, con guiones).
        if (!boletaId.toString().equals(id)) {
            return Optional.empty();
        }
        // Comparación en tiempo constante: no deja adivinar la firma midiendo cuánto tarda.
        boolean firmaCorrecta = MessageDigest.isEqual(
                firmar(id).getBytes(StandardCharsets.UTF_8), firma.getBytes(StandardCharsets.UTF_8));
        return firmaCorrecta ? Optional.of(boletaId) : Optional.empty();
    }

    String firmar(String contenido) {
        try {
            Mac mac = Mac.getInstance(ALGORITMO);
            mac.init(clave);
            byte[] firma = mac.doFinal(contenido.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(firma);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("No se pudo firmar el QR", e);
        }
    }
}