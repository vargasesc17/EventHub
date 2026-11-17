package com.eventhub.notificaciones.infrastructure.qr;

import com.eventhub.notificaciones.application.GeneradorImagenQr;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/** Dibuja el texto del QR como imagen PNG de 300x300 con la librería ZXing. */
@Component
public class ZxingGeneradorImagenQr implements GeneradorImagenQr {

    private static final int TAMANO = 300;

    @Override
    public byte[] generarPng(String contenido) {
        try {
            BitMatrix matriz = new QRCodeWriter().encode(contenido, BarcodeFormat.QR_CODE, TAMANO, TAMANO);
            ByteArrayOutputStream salida = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matriz, "PNG", salida);
            return salida.toByteArray();
        } catch (WriterException | IOException e) {
            throw new IllegalStateException("No se pudo generar la imagen del QR", e);
        }
    }
}
