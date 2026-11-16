package com.eventhub.notificaciones.infrastructure;

import com.eventhub.notificaciones.application.CompradorRepository;
import com.eventhub.notificaciones.application.EnviadorCorreo;
import com.eventhub.notificaciones.application.EventoRepository;
import com.eventhub.notificaciones.application.GeneradorImagenQr;
import com.eventhub.notificaciones.application.MensajeProcesadoRepository;
import com.eventhub.notificaciones.application.ProcesarBoletaCompradaUseCase;
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
}
