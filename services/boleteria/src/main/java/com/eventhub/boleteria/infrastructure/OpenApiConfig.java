package com.eventhub.boleteria.infrastructure;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI boleteriaOpenApi() {
        return new OpenAPI().info(new Info()
                .title("EventHub · Servicio de Boletería")
                .version("1.0")
                .description("""
                        Compra de boletas (Factory Method por tipo, Strategy por precio), consulta de aforo
                        y check-in con QR firmado (HMAC-SHA256, ADR-0004). El cupo se descuenta con un
                        UPDATE atómico (ADR-0003). Errores en formato ProblemDetail (RFC 9457)."""));
    }
}