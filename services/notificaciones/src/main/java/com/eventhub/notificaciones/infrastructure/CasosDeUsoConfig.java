package com.eventhub.notificaciones.infrastructure;

import com.eventhub.notificaciones.application.AlertaAforoRepository;
import com.eventhub.notificaciones.application.CompradorRepository;
import com.eventhub.notificaciones.application.EnviadorCorreo;
import com.eventhub.notificaciones.application.EnviarRecordatoriosUseCase;
import com.eventhub.notificaciones.application.EventoRepository;
import com.eventhub.notificaciones.application.GeneradorImagenQr;
import com.eventhub.notificaciones.application.MensajeProcesadoRepository;
import com.eventhub.notificaciones.application.ProcesarAforoCriticoUseCase;
import com.eventhub.notificaciones.application.ProcesarBoletaCompradaUseCase;
import com.eventhub.notificaciones.application.ProcesarEventoActualizadoUseCase;
import com.eventhub.notificaciones.application.RecordatorioRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Aquí se "conectan" los casos de uso con las implementaciones de sus puertos. Los casos de uso
 * no tienen anotaciones de Spring: así la capa application no depende del framework.
 */
@Configuration
public class CasosDeUsoConfig {

    @Bean
    Clock reloj() {
        return Clock.systemUTC();
    }

    @Bean
    ProcesarBoletaCompradaUseCase procesarBoletaCompradaUseCase(
            MensajeProcesadoRepository mensajes, EventoRepository eventos, CompradorRepository compradores,
            GeneradorImagenQr generadorQr, EnviadorCorreo enviadorCorreo, Clock reloj) {
        return new ProcesarBoletaCompradaUseCase(mensajes, eventos, compradores, generadorQr,
                enviadorCorreo, reloj);
    }

    @Bean
    ProcesarEventoActualizadoUseCase procesarEventoActualizadoUseCase(
            MensajeProcesadoRepository mensajes, EventoRepository eventos, CompradorRepository compradores,
            EnviadorCorreo enviadorCorreo, Clock reloj) {
        return new ProcesarEventoActualizadoUseCase(mensajes, eventos, compradores, enviadorCorreo, reloj);
    }

    @Bean
    ProcesarAforoCriticoUseCase procesarAforoCriticoUseCase(
            MensajeProcesadoRepository mensajes, AlertaAforoRepository alertas, EnviadorCorreo enviadorCorreo,
            Clock reloj) {
        return new ProcesarAforoCriticoUseCase(mensajes, alertas, enviadorCorreo, reloj);
    }

    @Bean
    EnviarRecordatoriosUseCase enviarRecordatoriosUseCase(
            RecordatorioRepository recordatorios, EnviadorCorreo enviadorCorreo, Clock reloj) {
        return new EnviarRecordatoriosUseCase(recordatorios, enviadorCorreo, reloj);
    }
}
