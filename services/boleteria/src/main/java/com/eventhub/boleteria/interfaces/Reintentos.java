package com.eventhub.boleteria.interfaces;

import com.eventhub.boleteria.interfaces.mensajes.MensajeInvalidoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;

import java.time.Duration;
import java.util.List;
import java.util.function.Consumer;

/**
 * Política de reintentos de los consumidores (docs/contratos, "Manejo de errores"):
 * <ul>
 *   <li>Error normal (por ejemplo, el servidor de correo no responde): se reintenta 3 veces,
 *       esperando 1 s, 2 s y 4 s. Si sigue fallando, el mensaje va a su DLQ.</li>
 *   <li>Mensaje inválido (no cumple el contrato): directo a la DLQ, sin reintentos.</li>
 * </ul>
 * "Ir a la DLQ" = lanzar AmqpRejectAndDontRequeueException: RabbitMQ rechaza el mensaje sin
 * devolverlo a la cola y, por el x-dead-letter-exchange de la cola, lo manda a la DLQ.
 */
public class Reintentos {

    private static final Logger log = LoggerFactory.getLogger(Reintentos.class);

    static final List<Duration> ESPERAS = List.of(Duration.ofSeconds(1), Duration.ofSeconds(2), Duration.ofSeconds(4));

    private final Consumer<Duration> esperar;

    /** En producción espera de verdad. */
    public Reintentos() {
        this(Reintentos::dormir);
    }

    /** Para los tests: permite no esperar. */
    Reintentos(Consumer<Duration> esperar) {
        this.esperar = esperar;
    }

    public void ejecutar(String descripcion, Runnable accion) {
        for (int intento = 0; ; intento++) {
            try {
                accion.run();
                return;
            } catch (MensajeInvalidoException e) {
                log.error("{}: mensaje inválido, va a la DLQ sin reintentos: {}", descripcion, e.getMessage());
                throw new AmqpRejectAndDontRequeueException(e.getMessage(), e);
            } catch (RuntimeException e) {
                if (intento == ESPERAS.size()) {
                    log.error("{}: falló después de {} reintentos, va a la DLQ: {}",
                            descripcion, ESPERAS.size(), e.getMessage());
                    throw new AmqpRejectAndDontRequeueException(e.getMessage(), e);
                }
                Duration espera = ESPERAS.get(intento);
                log.warn("{}: falló ({}). Reintento {} de {} en {} s",
                        descripcion, e.getMessage(), intento + 1, ESPERAS.size(), espera.toSeconds());
                esperar.accept(espera);
            }
        }
    }

    private static void dormir(Duration espera) {
        try {
            Thread.sleep(espera.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AmqpRejectAndDontRequeueException("Interrumpido mientras esperaba un reintento", e);
        }
    }
}
