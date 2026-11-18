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

    public static String asuntoCambio(InfoEvento evento, boolean cancelado) {
        return (cancelado ? "Evento cancelado: " : "Cambio en tu evento: ") + evento.nombre();
    }

    /**
     * Un solo correo con todos los cambios (fecha y/o lugar), para que cada comprador reciba una
     * sola alerta (RF-04). fechaAnterior y lugarAnterior son null si eso no cambió.
     */
    public static String cuerpoCambio(String nombre, InfoEvento evento, boolean cancelado,
                                      Instant fechaAnterior, String lugarAnterior) {
        if (cancelado) {
            return """
                    <h2>Hola, %s</h2>
                    <p>Lamentamos informarte que el evento <b>%s</b>, programado para el %s en %s,
                    fue <b>cancelado</b>.</p>
                    <p>Tu boleta ya no es válida para el ingreso.</p>
                    """.formatted(escapar(nombre), escapar(evento.nombre()), fecha(evento.fechaInicio()),
                    escapar(evento.lugar()));
        }
        StringBuilder cambios = new StringBuilder("<ul>");
        if (fechaAnterior != null) {
            cambios.append("<li><b>Fecha:</b> antes el ").append(fecha(fechaAnterior))
                    .append(", ahora el <b>").append(fecha(evento.fechaInicio())).append("</b></li>");
        }
        if (lugarAnterior != null) {
            cambios.append("<li><b>Lugar:</b> antes en ").append(escapar(lugarAnterior))
                    .append(", ahora en <b>").append(escapar(evento.lugar())).append("</b></li>");
        }
        cambios.append("</ul>");
        return """
                <h2>Hola, %s</h2>
                <p>El evento <b>%s</b> tuvo cambios:</p>
                %s
                <p>Tu boleta sigue siendo válida. Te enviaremos un recordatorio 24 horas antes.</p>
                """.formatted(escapar(nombre), escapar(evento.nombre()), cambios);
    }

    public static String asuntoRecordatorio(InfoEvento evento) {
        return "Mañana es " + evento.nombre();
    }

    public static String cuerpoRecordatorio(String nombre, InfoEvento evento) {
        return """
                <h2>Hola, %s</h2>
                <p>Te recordamos que <b>%s</b> es el %s en %s.</p>
                <p>Lleva el código QR que te enviamos al comprar tu boleta.</p>
                """.formatted(escapar(nombre), escapar(evento.nombre()), fecha(evento.fechaInicio()),
                escapar(evento.lugar()));
    }

    public static String asuntoAforo(String nombreEvento) {
        return "Aforo crítico: " + nombreEvento;
    }

    public static String cuerpoAforo(String nombreEvento, int vendidas, int aforoMaximo,
                                     double porcentaje, int umbral) {
        return """
                <h2>Alerta de aforo</h2>
                <p>Tu evento <b>%s</b> alcanzó el %d %% de su aforo.</p>
                <p>Boletas vendidas: <b>%d de %d</b> (%s %%).</p>
                """.formatted(escapar(nombreEvento), umbral, vendidas, aforoMaximo,
                String.format(Locale.forLanguageTag("es-CO"), "%.1f", porcentaje));
    }

    /** Evita que un nombre con caracteres como < rompa el HTML del correo. */
    static String escapar(String texto) {
        return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
