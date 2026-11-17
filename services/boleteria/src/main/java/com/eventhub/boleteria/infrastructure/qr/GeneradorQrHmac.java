package com.eventhub.boleteria.infrastructure.qr;

import com.eventhub.boleteria.application.GeneradorQr;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.UUID;

/**
 * ADR-0004: el QR es "boletaId.firma", con firma = HMAC-SHA256(secreto, boletaId).
 * Sin el secreto nadie puede fabricar un QR válido; con él, se verifica sin consultar la base.
 */
@Component
public class GeneradorQrHmac implements GeneradorQr {

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