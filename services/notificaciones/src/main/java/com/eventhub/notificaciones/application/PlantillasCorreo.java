package com.eventhub.notificaciones.application;

import com.eventhub.notificaciones.domain.InfoEvento;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Textos de los correos. Están en un solo lugar para que cambiar la redacción no obligue a tocar
 * los casos de uso (SRP). Las fechas se muestran en hora de Colombia.
 */
public final class PlantillasCorreo {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter
            .ofPattern("EEEE d 'de' MMMM 'de' yyyy, h:mm a", Locale.forLanguageTag("es-CO"))
            .withZone(ZoneId.of("America/Bogota"));

    private PlantillasCorreo() { }

    public static String fecha(Instant instante) {
        return FORMATO.format(instante);
    }

    public static String asuntoConfirmacion(InfoEvento evento) {
        return "Tu boleta para " + evento.nombre();
    }

    public static String cuerpoConfirmacion(String nombre, InfoEvento evento, String categoria) {
        return """
                <h2>¡Hola, %s!</h2>
                <p>Tu compra quedó confirmada.</p>
                <ul>
                  <li><b>Evento:</b> %s</li>
                  <li><b>Fecha:</b> %s</li>
                  <li><b>Lugar:</b> %s</li>
                  <li><b>Categoría:</b> %s</li>
                </ul>
                <p>Presenta este código QR en la entrada:</p>
                <img src="cid:qr" alt="Código QR de tu boleta" width="240" height="240">
                <p>El QR es de un solo uso.</p>
                """.formatted(escapar(nombre), escapar(evento.nombre()), fecha(evento.fechaInicio()),
                escapar(evento.lugar()), escapar(categoria));
    }

    /** Evita que un nombre con caracteres como < rompa el HTML del correo. */
    static String escapar(String texto) {
        return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
