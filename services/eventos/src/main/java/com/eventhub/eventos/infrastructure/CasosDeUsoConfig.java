package com.eventhub.eventos.infrastructure;

import com.eventhub.eventos.application.ConsultarEventosUseCase;
import com.eventhub.eventos.application.EventoRepository;
import com.eventhub.eventos.application.GestionarEventoUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CasosDeUsoConfig {

    @Bean
    GestionarEventoUseCase gestionarEventoUseCase(EventoRepository repositorio) {
        return new GestionarEventoUseCase(repositorio);
    }

    @Bean
    ConsultarEventosUseCase consultarEventosUseCase(EventoRepository repositorio) {
        return new ConsultarEventosUseCase(repositorio);
    }
}