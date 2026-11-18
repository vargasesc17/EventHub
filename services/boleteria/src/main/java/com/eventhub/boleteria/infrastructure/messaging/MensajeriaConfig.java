package com.eventhub.boleteria.infrastructure.messaging;

import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MensajeriaConfig {

    @Bean
    MessageConverter conversorJson() {
        return new JacksonJsonMessageConverter();
    }
}