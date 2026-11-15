package com.eventhub.notificaciones.infrastructure.messaging;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Criterio de aceptación de #18: al arrancar, el servicio se conecta a RabbitMQ
 * y registra en el log un mensaje de prueba. Envía un texto a la cola
 * notificaciones.ping y lo vuelve a recibir: si aparece "conexión OK" en el log,
 * la ida y la vuelta por RabbitMQ funcionan.
 *
 * No corre en los tests (perfil "test"), porque ahí no hay RabbitMQ.
 **/
@Component
@Profile("!test")
public class MensajeDePrueba implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(MensajeDePrueba.class);

    private final RabbitTemplate rabbitTemplate;

    public MensajeDePrueba(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        String texto = "Hola desde Notificaciones, " + Instant.now();
        try {
            rabbitTemplate.convertAndSend("", TopologiaRabbit.COLA_PING, texto);
            log.info("Mensaje de prueba enviado a RabbitMQ: {}", texto);
        } catch (AmqpException e) {
            log.error("No se pudo conectar a RabbitMQ: {}", e.getMessage());
        }
    }

    @RabbitListener(queues = TopologiaRabbit.COLA_PING)
    public void recibir(String texto) {
        log.info("Mensaje de prueba recibido de RabbitMQ: {} -> conexión OK", texto);
    }
}
