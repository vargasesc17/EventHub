package com.eventhub.boleteria.infrastructure;

import com.eventhub.boleteria.application.AforoRepository;
import com.eventhub.boleteria.application.InventarioRepository;
import com.eventhub.boleteria.application.MensajeProcesadoRepository;
import com.eventhub.boleteria.application.SincronizarEventoUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class CasosDeUsoConfig {

    @Bean
    Clock reloj() {
        return Clock.systemUTC();
    }

    @Bean
    SincronizarEventoUseCase sincronizarEventoUseCase(MensajeProcesadoRepository mensajes, AforoRepository aforos,
                                                      InventarioRepository inventarios, Clock reloj) {
        return new SincronizarEventoUseCase(mensajes, aforos, inventarios, reloj);
    }
}