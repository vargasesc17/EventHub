package com.eventhub.boleteria.infrastructure.messaging;

import com.eventhub.boleteria.application.PublicadorBoleteria;
import com.eventhub.boleteria.domain.AforoEvento;
import com.eventhub.boleteria.domain.Boleta;
import com.eventhub.boleteria.domain.Compra;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.UUID;

/**
 * Publica los mensajes de Boletería. Si hay una transacción abierta, espera a que se confirme:
 * una compra que se deshace nunca llega a avisarse.
 */
@Component
@Profile("!test")
public class RabbitPublicadorBoleteria implements PublicadorBoleteria {

    private static final Logger log = LoggerFactory.getLogger(RabbitPublicadorBoleteria.class);

    private final RabbitTemplate rabbitTemplate;

    public RabbitPublicadorBoleteria(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void boletaComprada(Boleta boleta, Compra compra, AforoEvento evento, String codigoQr) {
        var mensaje = BoletaCompradaMensaje.desde(boleta, compra, evento, codigoQr);
        despuesDelCommit(() -> enviar(BoletaCompradaMensaje.TIPO, mensaje.messageId(), mensaje));
    }

    @Override
    public void aforoCritico(AforoEvento evento, int umbral) {
        var mensaje = AforoCriticoMensaje.desde(evento, umbral);
        despuesDelCommit(() -> enviar(AforoCriticoMensaje.TIPO, mensaje.messageId(), mensaje));
    }

    private void despuesDelCommit(Runnable envio) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    envio.run();
                }
            });
        } else {
            envio.run();
        }
    }

    private void enviar(String routingKey, UUID messageId, Object mensaje) {
        try {
            rabbitTemplate.convertAndSend(TopologiaRabbit.EXCHANGE, routingKey, mensaje, m -> {
                m.getMessageProperties().setMessageId(messageId.toString());
                // Encabezado propio de Spring con el nombre de la clase Java: no es parte del contrato.
                m.getMessageProperties().getHeaders().remove("__TypeId__");
                return m;
            });
            log.info("Publicado {} (messageId {})", routingKey, messageId);
        } catch (AmqpException e) {
            // La compra ya quedó confirmada; el mensaje se pierde. Riesgo conocido (docs/contratos).
            log.error("No se pudo publicar {} (messageId {}): {}", routingKey, messageId, e.getMessage());
        }
    }
}