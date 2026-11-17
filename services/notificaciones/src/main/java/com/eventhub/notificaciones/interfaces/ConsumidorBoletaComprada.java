package com.eventhub.notificaciones.interfaces;

import com.eventhub.notificaciones.application.BoletaCompradaComando;
import com.eventhub.notificaciones.application.ProcesarBoletaCompradaUseCase;
import com.eventhub.notificaciones.application.Resultado;
import com.eventhub.notificaciones.interfaces.mensajes.LectorMensajes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.nio.charset.StandardCharsets;

/**
 * #25 · Escucha la cola notificaciones.boleta-comprada (la declara TopologiaRabbit, #18).
 *
 * <p>Si Notificaciones estaba apagado, los mensajes esperaron en la cola: al arrancar, este
 * listener los recibe uno por uno (RNF-02).
 *
 * <p>Cada intento va en una transacción: si el correo falla, no queda nada guardado a medias.
 * Solo cuando el método termina sin error, el mensaje se confirma (ack) y sale de la cola.
 */
@Component
public class ConsumidorBoletaComprada {

    private static final Logger log = LoggerFactory.getLogger(ConsumidorBoletaComprada.class);

    private final LectorMensajes lector;
    private final ProcesarBoletaCompradaUseCase casoDeUso;
    private final TransactionTemplate transaccion;
    private final Reintentos reintentos = new Reintentos();

    public ConsumidorBoletaComprada(LectorMensajes lector, ProcesarBoletaCompradaUseCase casoDeUso,
                                    TransactionTemplate transaccion) {
        this.lector = lector;
        this.casoDeUso = casoDeUso;
        this.transaccion = transaccion;
    }

    // El nombre va escrito aquí porque interfaces no puede depender de infrastructure (ArquitecturaTest).
    @RabbitListener(queues = "notificaciones.boleta-comprada")
    public void recibir(Message mensaje) {
        reintentos.ejecutar("boleta.comprada", () -> {
            String texto = new String(mensaje.getBody(), StandardCharsets.UTF_8);
            BoletaCompradaComando comando = lector.leerBoletaComprada(texto);
            Resultado resultado = transaccion.execute(estado -> casoDeUso.ejecutar(comando));
            log.info("boleta.comprada {} (boleta {}, para {}): {}", comando.messageId(),
                    comando.boletaId(), comando.correoComprador(), resultado);
        });
    }
}
