package com.eventhub.boleteria.infrastructure.messaging;

import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** La cola de Boletería para evento.actualizado, tal como está en docs/contratos (#15). */
@Configuration
public class TopologiaRabbit {

    public static final String EXCHANGE = "eventhub.events";
    public static final String DLX = "eventhub.dlx";
    public static final String COLA_EVENTO_ACTUALIZADO = "boleteria.evento-actualizado";

    @Bean
    public Declarables topologiaBoleteria() {
        TopicExchange exchange = new TopicExchange(EXCHANGE, true, false);
        DirectExchange dlx = new DirectExchange(DLX, true, false);
        String dlq = COLA_EVENTO_ACTUALIZADO + ".dlq";

        Queue cola = QueueBuilder.durable(COLA_EVENTO_ACTUALIZADO)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", dlq)
                .build();
        Queue muertos = QueueBuilder.durable(dlq).build();

        return new Declarables(exchange, dlx, cola, muertos,
                BindingBuilder.bind(cola).to(exchange).with("evento.actualizado"),
                BindingBuilder.bind(muertos).to(dlx).with(dlq));
    }
}