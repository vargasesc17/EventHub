package com.eventhub.boleteria.infrastructure;

import com.eventhub.boleteria.application.RegistroFabricas;
import com.eventhub.boleteria.domain.FabricaBoleta;
import com.eventhub.boleteria.domain.FabricaBoletaCortesia;
import com.eventhub.boleteria.domain.FabricaBoletaEstudiante;
import com.eventhub.boleteria.domain.FabricaBoletaGeneral;
import com.eventhub.boleteria.domain.FabricaBoletaVIP;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class FabricasConfig {

    @Bean FabricaBoleta fabricaBoletaGeneral() { return new FabricaBoletaGeneral(); }
    @Bean FabricaBoleta fabricaBoletaVIP() { return new FabricaBoletaVIP(); }
    @Bean FabricaBoleta fabricaBoletaEstudiante() { return new FabricaBoletaEstudiante(); }
    @Bean FabricaBoleta fabricaBoletaCortesia() { return new FabricaBoletaCortesia(); }

    /** Spring entrega aquí TODAS las fábricas declaradas: una nueva queda registrada sola. */
    @Bean
    RegistroFabricas registroFabricas(List<FabricaBoleta> fabricas) {
        return new RegistroFabricas(fabricas);
    }
}