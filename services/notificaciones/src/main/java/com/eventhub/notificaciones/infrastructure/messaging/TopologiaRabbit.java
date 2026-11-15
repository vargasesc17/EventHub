package com.eventhub.notificaciones.infrastructure.messaging;

import java.util.ArrayList;
import java.util.List;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarable;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Exchanges, colas y bindings de Notificaciones, tal como están en
 * docs/contratos/README.md (#15).
 *
 * Según el contrato, el consumidor declara sus colas, sus DLQ y los bindings al
 * arrancar; así cada servicio puede arrancar en cualquier orden. Declarar el
 * exchange también aquí es seguro: si ya existe con la misma configuración,
 * RabbitMQ no hace nada.
 **/
@Configuration
public class TopologiaRabbit {

    public static final String EXCHANGE = "eventhub.events";
    public static final String DLX = "eventhub.dlx";

    public static final String RK_BOLETA_COMPRADA = "boleta.comprada";
    public static final String RK_EVENTO_ACTUALIZADO = "evento.actualizado";
    public static final String RK_AFORO_CRITICO = "evento.aforo.critico";

    public static final String COLA_BOLETA_COMPRADA = "notificaciones.boleta-comprada";
    public static final String COLA_EVENTO_ACTUALIZADO = "notificaciones.evento-actualizado";
    public static final String COLA_AFORO_CRITICO = "notificaciones.aforo-critico";

    /** Cola temporal, solo para comprobar la conexión al arrancar (#18). */
    public static final String COLA_PING = "notificaciones.ping";

    @Bean
    public Declarables topologiaNotificaciones() {
        TopicExchange exchange = new TopicExchange(EXCHANGE, true, false);
        DirectExchange dlx = new DirectExchange(DLX, true, false);

        List<Declarable> declarables = new ArrayList<>();
        declarables.add(exchange);
        declarables.add(dlx);
        agregarColaConDlq(declarables, exchange, dlx, COLA_BOLETA_COMPRADA, RK_BOLETA_COMPRADA);
        agregarColaConDlq(declarables, exchange, dlx, COLA_EVENTO_ACTUALIZADO, RK_EVENTO_ACTUALIZADO);
        agregarColaConDlq(declarables, exchange, dlx, COLA_AFORO_CRITICO, RK_AFORO_CRITICO);
        // No durable y se borra sola al apagar el servicio: solo sirve para el mensaje de prueba.
        declarables.add(new Queue(COLA_PING, false, false, true));
        return new Declarables(declarables);
    }

    /** Cola durable con su cola de mensajes muertos (DLQ), según el contrato. */
    private static void agregarColaConDlq(List<Declarable> declarables, TopicExchange exchange,
                                          DirectExchange dlx, String cola, String routingKey) {
        String dlq = cola + ".dlq";
        Queue principal = QueueBuilder.durable(cola)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", dlq)
                .build();
        Queue muertos = QueueBuilder.durable(dlq).build();
        declarables.add(principal);
        declarables.add(muertos);
        declarables.add(BindingBuilder.bind(principal).to(exchange).with(routingKey));
        declarables.add(BindingBuilder.bind(muertos).to(dlx).with(dlq));
    }
}
