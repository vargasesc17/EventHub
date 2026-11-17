package com.eventhub.eventos.infrastructure;

import com.eventhub.eventos.application.ConsultarEventosUseCase;
import com.eventhub.eventos.application.EventoRepository;
import com.eventhub.eventos.application.GestionarEventoUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.eventhub.eventos.application.PublicadorEventos;

@Configuration
public class CasosDeUsoConfig {

    @Bean
    GestionarEventoUseCase gestionarEventoUseCase(EventoRepository repositorio, PublicadorEventos publicador) {
        return new GestionarEventoUseCase(repositorio, publicador);
    }

    @Bean
    ConsultarEventosUseCase consultarEventosUseCase(EventoRepository repositorio) {
        return new ConsultarEventosUseCase(repositorio);
    }
}