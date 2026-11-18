package com.eventhub.notificaciones.interfaces;

import com.eventhub.notificaciones.application.AforoCriticoComando;
import com.eventhub.notificaciones.application.ProcesarAforoCriticoUseCase;
import com.eventhub.notificaciones.application.Resultado;
import com.eventhub.notificaciones.interfaces.mensajes.LectorMensajes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.nio.charset.StandardCharsets;

/** #26 · Escucha la cola notificaciones.aforo-critico (la declara TopologiaRabbit, #18). */
@Component
public class ConsumidorAforoCritico {

    private static final Logger log = LoggerFactory.getLogger(ConsumidorAforoCritico.class);

    private final LectorMensajes lector;
    private final ProcesarAforoCriticoUseCase casoDeUso;
    private final TransactionTemplate transaccion;
    private final Reintentos reintentos = new Reintentos();

    public ConsumidorAforoCritico(LectorMensajes lector, ProcesarAforoCriticoUseCase casoDeUso,
                                  TransactionTemplate transaccion) {
        this.lector = lector;
        this.casoDeUso = casoDeUso;
        this.transaccion = transaccion;
    }

    @RabbitListener(queues = "notificaciones.aforo-critico")
    public void recibir(Message mensaje) {
        reintentos.ejecutar("evento.aforo.critico", () -> {
            String texto = new String(mensaje.getBody(), StandardCharsets.UTF_8);
            AforoCriticoComando comando = lector.leerAforoCritico(texto);
            Resultado resultado = transaccion.execute(estado -> casoDeUso.ejecutar(comando));
            log.info("evento.aforo.critico {} (evento {}, {} de {} vendidas, para {}): {}", comando.messageId(),
                    comando.eventoId(), comando.vendidas(), comando.aforoMaximo(),
                    comando.correoOrganizador(), resultado);
        });
    }
}
