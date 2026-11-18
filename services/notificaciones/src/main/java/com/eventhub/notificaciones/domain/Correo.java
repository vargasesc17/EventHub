package com.eventhub.notificaciones.domain;

/**
 * Un correo listo para enviar.
 *
 * @param qrPng imagen PNG del QR para incrustar en el correo, o null si no lleva QR.
 *              En el HTML se referencia como {@code <img src="cid:qr">}.
 */
public record Correo(String para, String asunto, String cuerpoHtml, byte[] qrPng) {

    public Correo {
        if (para == null || para.isBlank() || asunto == null || cuerpoHtml == null) {
            throw new IllegalArgumentException("El correo necesita destinatario, asunto y cuerpo");
        }
    }

    /** Un correo sin imagen del QR (alertas y recordatorios). */
    public static Correo sinQr(String para, String asunto, String cuerpoHtml) {
        return new Correo(para, asunto, cuerpoHtml, null);
    }

    public boolean tieneQr() {
        return qrPng != null;
    }
}
