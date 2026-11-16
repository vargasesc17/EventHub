package com.eventhub.boleteria.infrastructure;

import com.eventhub.boleteria.application.RegistroEstrategias;
import com.eventhub.boleteria.domain.PrecioDinamico;
import com.eventhub.boleteria.domain.PrecioEarlyBird;
import com.eventhub.boleteria.domain.PrecioFijo;
import com.eventhub.boleteria.domain.PrecioStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class EstrategiasConfig {

    @Bean PrecioStrategy precioFijo() { return new PrecioFijo(); }
    @Bean PrecioStrategy precioEarlyBird() { return new PrecioEarlyBird(); }
    @Bean PrecioStrategy precioDinamico() { return new PrecioDinamico(); }

    @Bean
    RegistroEstrategias registroEstrategias(List<PrecioStrategy> estrategias) {
        return new RegistroEstrategias(estrategias);
    }
}