package com.eventhub.boleteria.infrastructure;

import com.eventhub.boleteria.application.*;
import org.springframework.beans.factory.annotation.Value;
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
                                                      InventarioRepository inventarios, BoletaRepository boletas,
                                                      Clock reloj) {
        return new SincronizarEventoUseCase(mensajes, aforos, inventarios, boletas, reloj);
    }

    @Bean
    ComprarBoletaUseCase comprarBoletaUseCase(AforoRepository aforos, InventarioRepository inventarios,
                                              BoletaRepository boletas, RegistroEstrategias estrategias,
                                              RegistroFabricas fabricas, PasarelaPagos pasarela,
                                              GeneradorQr generadorQr, PublicadorBoleteria publicador,
                                              Clock reloj,
                                              @Value("${boleteria.alerta.umbral:90}") int umbralAlerta) {
        return new ComprarBoletaUseCase(aforos, inventarios, boletas, estrategias, fabricas, pasarela,
                generadorQr, publicador, reloj, umbralAlerta);
    }

    @Bean
    CheckInUseCase checkInUseCase(ValidadorQr validadorQr, BoletaRepository boletas, CheckInRepository checkIns,
                                  Clock reloj) {
        return new CheckInUseCase(validadorQr, boletas, checkIns, reloj);
    }

    @Bean
    ConsultarAforoUseCase consultarAforoUseCase(AforoRepository aforos, InventarioRepository inventarios) {
        return new ConsultarAforoUseCase(aforos, inventarios);
    }
}