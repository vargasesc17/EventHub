package com.eventhub.notificaciones.interfaces;

import com.eventhub.notificaciones.application.EnviarRecordatoriosUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * #26 · Tarea programada: cada minuto busca y envía los recordatorios de 24 horas (RF-04).
 * Es una "entrada" al sistema igual que un consumidor, solo que la dispara el reloj en vez de
 * RabbitMQ. La programación se activa en infrastructure (ProgramacionConfig).
 */
@Component
public class RecordatoriosJob {

    private static final Logger log = LoggerFactory.getLogger(RecordatoriosJob.class);

    private final EnviarRecordatoriosUseCase casoDeUso;

    public RecordatoriosJob(EnviarRecordatoriosUseCase casoDeUso) {
        this.casoDeUso = casoDeUso;
    }

    @Scheduled(initialDelayString = "PT30S", fixedDelayString = "${notificaciones.recordatorios.intervalo:PT1M}")
    public void enviarPendientes() {
        int enviados = casoDeUso.ejecutar();
        if (enviados > 0) {
            log.info("Recordatorios de 24 horas enviados: {}", enviados);
        }
    }
}
