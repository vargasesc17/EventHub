package com.eventhub.eventos.infrastructure;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI eventosOpenApi() {
        return new OpenAPI().info(new Info()
                .title("EventHub · Servicio de Eventos")
                .version("1.0")
                .description("""
                        Creación y ciclo de vida de los eventos (BORRADOR → PUBLICADO → EN_CURSO → FINALIZADO,
                        o CANCELADO). Cada cambio relevante publica `evento.actualizado` en RabbitMQ.
                        Errores en formato ProblemDetail (RFC 9457): 400 campos inválidos, 404 no existe,
                        422 regla de negocio violada."""));
    }
}