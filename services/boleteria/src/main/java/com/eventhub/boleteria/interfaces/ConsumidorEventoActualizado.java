package com.eventhub.boleteria.interfaces;

import com.eventhub.boleteria.application.EventoActualizadoComando;
import com.eventhub.boleteria.application.Resultado;
import com.eventhub.boleteria.application.SincronizarEventoUseCase;
import com.eventhub.boleteria.interfaces.mensajes.LectorMensajes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.nio.charset.StandardCharsets;

/**
 * Escucha la cola boleteria.evento-actualizado. Cada intento va en una transacción: o se aplica
 * el mensaje completo (copia del evento, inventario y registro del messageId) o no queda nada.
 */
@Component
public class ConsumidorEventoActualizado {

    private static final Logger log = LoggerFactory.getLogger(ConsumidorEventoActualizado.class);

    private final LectorMensajes lector;
    private final SincronizarEventoUseCase casoDeUso;
    private final TransactionTemplate transaccion;
    private final Reintentos reintentos = new Reintentos();

    public ConsumidorEventoActualizado(LectorMensajes lector, SincronizarEventoUseCase casoDeUso,
                                       TransactionTemplate transaccion) {
        this.lector = lector;
        this.casoDeUso = casoDeUso;
        this.transaccion = transaccion;
    }

    // El nombre va escrito aquí porque interfaces no puede depender de infrastructure (ArquitecturaTest).
    @RabbitListener(queues = "boleteria.evento-actualizado")
    public void recibir(Message mensaje) {
        reintentos.ejecutar("evento.actualizado", () -> {
            String texto = new String(mensaje.getBody(), StandardCharsets.UTF_8);
            EventoActualizadoComando comando = lector.leerEventoActualizado(texto);
            Resultado resultado = transaccion.execute(estado -> casoDeUso.ejecutar(comando));
            log.info("evento.actualizado {} (evento {}, cambios {}): {}", comando.messageId(),
                    comando.eventoId(), comando.cambios(), resultado);
        });
    }
}