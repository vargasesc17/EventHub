package com.eventhub.eventos.infrastructure.messaging;

import com.eventhub.eventos.application.PublicadorEventos;
import com.eventhub.eventos.domain.Evento;
import com.eventhub.eventos.domain.TipoCambio;
import com.eventhub.eventos.domain.ValoresAnteriores;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@Profile("!test")
public class RabbitPublicadorEventos implements PublicadorEventos {

    private static final Logger log = LoggerFactory.getLogger(RabbitPublicadorEventos.class);

    private final RabbitTemplate rabbitTemplate;

    public RabbitPublicadorEventos(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void eventoActualizado(Evento evento, Set<TipoCambio> cambios, ValoresAnteriores anteriores) {
        var mensaje = EventoActualizadoMensaje.desde(evento, cambios, anteriores);
        try {
            rabbitTemplate.convertAndSend(MensajeriaConfig.EXCHANGE, EventoActualizadoMensaje.TIPO, mensaje, m -> {
                m.getMessageProperties().setMessageId(mensaje.messageId().toString());
                // Encabezado propio de Spring con el nombre de esta clase Java: no es parte del contrato.
                m.getMessageProperties().getHeaders().remove("__TypeId__");
                return m;
            });
            log.info("Publicado evento.actualizado {} del evento {} (messageId {})",
                    mensaje.datos().cambios(), evento.getId(), mensaje.messageId());
        } catch (AmqpException e) {
            // El cambio ya quedó guardado; el mensaje se pierde. Riesgo conocido (ver docs/contratos).
            log.error("No se pudo publicar evento.actualizado del evento {}: {}", evento.getId(), e.getMessage());
        }
    }
}