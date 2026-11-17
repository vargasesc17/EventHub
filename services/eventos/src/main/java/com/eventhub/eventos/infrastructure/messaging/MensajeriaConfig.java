package com.eventhub.eventos.infrastructure.messaging;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MensajeriaConfig {

    public static final String EXCHANGE = "eventhub.events";

    /** Según el contrato, el publicador declara el exchange: topic y durable, igual que en Notificaciones. */
    @Bean
    TopicExchange exchangeDeEventos() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    /** Hace que RabbitTemplate envíe los objetos como JSON (content_type application/json). */
    @Bean
    MessageConverter conversorJson() {
        return new JacksonJsonMessageConverter();
    }
}