package com.eventhub.notificaciones.infrastructure.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;

/** Comprueba que la topología declarada coincide con docs/contratos/README.md (#15). */
class TopologiaRabbitTest {

    private final Declarables declarables = new TopologiaRabbit().topologiaNotificaciones();

    @Test
    void declaraElExchangeTopicYElDeadLetterExchange() {
        List<Exchange> exchanges = declarables.getDeclarablesByType(Exchange.class);
        assertThat(exchanges).extracting(Exchange::getName)
                .containsExactlyInAnyOrder("eventhub.events", "eventhub.dlx");
        assertThat(exchanges).allMatch(Exchange::isDurable);
    }

    @Test
    void cadaColaEsDurableYTieneSuDlq() {
        for (String cola : List.of("notificaciones.boleta-comprada",
                "notificaciones.evento-actualizado", "notificaciones.aforo-critico")) {
            Queue principal = cola(cola);
            assertThat(principal.isDurable()).isTrue();
            Map<String, Object> args = principal.getArguments();
            assertThat(args).containsEntry("x-dead-letter-exchange", "eventhub.dlx")
                    .containsEntry("x-dead-letter-routing-key", cola + ".dlq");
            assertThat(cola(cola + ".dlq").isDurable()).isTrue();
        }
    }

    @Test
    void cadaColaRecibeSuRoutingKey() {
        assertThat(declarables.getDeclarablesByType(Binding.class))
                .filteredOn(b -> b.getExchange().equals("eventhub.events"))
                .extracting(Binding::getDestination, Binding::getRoutingKey)
                .containsExactlyInAnyOrder(
                        tuple("notificaciones.boleta-comprada", "boleta.comprada"),
                        tuple("notificaciones.evento-actualizado", "evento.actualizado"),
                        tuple("notificaciones.aforo-critico", "evento.aforo.critico"));
    }

    private Queue cola(String nombre) {
        return declarables.getDeclarablesByType(Queue.class).stream()
                .filter(q -> q.getName().equals(nombre))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No se declaró la cola " + nombre));
    }
}
