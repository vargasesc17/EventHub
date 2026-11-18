package com.eventhub.notificaciones.application;

import com.eventhub.notificaciones.domain.Correo;
import com.eventhub.notificaciones.domain.Recordatorio;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/**
 * #26 · Recordatorio 24 horas antes del evento (RF-04). Lo ejecuta RecordatoriosJob cada minuto.
 *
 * <p>Busca los compradores de eventos que empiezan en las próximas 24 horas y que no han recibido
 * el recordatorio para la fecha ACTUAL del evento. Si el evento cambia de fecha, la nueva fecha
 * genera su propio recordatorio; si se cancela, ya no aparece.
 *
 * <p>Cada recordatorio se marca apenas se envía: si un correo falla, los demás no se repiten y el
 * fallido se intenta otra vez en la siguiente ejecución.
 */
public class EnviarRecordatoriosUseCase {

    public static final Duration ANTICIPACION = Duration.ofHours(24);

    private final RecordatorioRepository recordatorios;
    private final EnviadorCorreo enviadorCorreo;
    private final Clock reloj;

    public EnviarRecordatoriosUseCase(RecordatorioRepository recordatorios, EnviadorCorreo enviadorCorreo,
                                      Clock reloj) {
        this.recordatorios = recordatorios;
        this.enviadorCorreo = enviadorCorreo;
        this.reloj = reloj;
    }

    /** Devuelve cuántos recordatorios se enviaron. */
    public int ejecutar() {
        Instant ahora = reloj.instant();
        int enviados = 0;
        for (Recordatorio r : recordatorios.pendientes(ahora, ahora.plus(ANTICIPACION))) {
            try {
                enviadorCorreo.enviar(Correo.sinQr(r.correo(),
                        PlantillasCorreo.asuntoRecordatorio(r.evento()),
                        PlantillasCorreo.cuerpoRecordatorio(r.nombre(), r.evento())));
                recordatorios.marcarEnviado(r.boletaId(), r.evento().fechaInicio(), reloj.instant());
                enviados++;
            } catch (RuntimeException e) {
                // Se reintenta en la próxima ejecución; no detiene los demás recordatorios.
            }
        }
        return enviados;
    }
}
