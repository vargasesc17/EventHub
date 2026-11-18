package com.eventhub.notificaciones.interfaces;

import com.eventhub.notificaciones.application.EventoActualizadoComando;
import com.eventhub.notificaciones.application.ProcesarEventoActualizadoUseCase;
import com.eventhub.notificaciones.application.Resultado;
import com.eventhub.notificaciones.interfaces.mensajes.LectorMensajes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.nio.charset.StandardCharsets;

/** #26 · Escucha la cola notificaciones.evento-actualizado (la declara TopologiaRabbit, #18). */
@Component
public class ConsumidorEventoActualizado {

    private static final Logger log = LoggerFactory.getLogger(ConsumidorEventoActualizado.class);

    private final LectorMensajes lector;
    private final ProcesarEventoActualizadoUseCase casoDeUso;
    private final TransactionTemplate transaccion;
    private final Reintentos reintentos = new Reintentos();

    public ConsumidorEventoActualizado(LectorMensajes lector, ProcesarEventoActualizadoUseCase casoDeUso,
                                       TransactionTemplate transaccion) {
        this.lector = lector;
        this.casoDeUso = casoDeUso;
        this.transaccion = transaccion;
    }

    // El nombre va escrito aquí porque interfaces no puede depender de infrastructure (ArquitecturaTest).
    @RabbitListener(queues = "notificaciones.evento-actualizado")
    public void recibir(Message mensaje) {
        reintentos.ejecutar("evento.actualizado", () -> {
            String texto = new String(mensaje.getBody(), StandardCharsets.UTF_8);
            EventoActualizadoComando comando = lector.leerEventoActualizado(texto);
            Resultado resultado = transaccion.execute(estado -> casoDeUso.ejecutar(comando));
            log.info("evento.actualizado {} (evento {}, cambios {}): {}", comando.messageId(),
                    comando.evento().eventoId(), comando.cambios(), resultado);
        });
    }
}
