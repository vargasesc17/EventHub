package com.eventhub.boleteria.interfaces;

import com.eventhub.boleteria.interfaces.mensajes.MensajeInvalidoException;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/** Política del contrato: 3 reintentos (1 s, 2 s, 4 s) y luego DLQ; inválidos directo a la DLQ. */
class ReintentosTest {

    private final List<Duration> esperas = new ArrayList<>();
    private final Reintentos reintentos = new Reintentos(esperas::add);   // no espera de verdad
    private final AtomicInteger intentos = new AtomicInteger();

    @Test
    void siFuncionaAlPrimerIntentoNoEspera() {
        reintentos.ejecutar("prueba", intentos::incrementAndGet);

        assertEquals(1, intentos.get());
        assertTrue(esperas.isEmpty());
    }

    @Test
    void siFallaUnaVezReintentaYFunciona() {
        reintentos.ejecutar("prueba", () -> {
            if (intentos.incrementAndGet() == 1) {
                throw new IllegalStateException("El servidor de correo no responde");
            }
        });

        assertEquals(2, intentos.get());
        assertEquals(List.of(Duration.ofSeconds(1)), esperas);
    }

    @Test
    void siSigueFallandoReintenta3VecesYLuegoVaALaDlq() {
        assertThrows(AmqpRejectAndDontRequeueException.class, () -> reintentos.ejecutar("prueba", () -> {
            intentos.incrementAndGet();
            throw new IllegalStateException("El servidor de correo no responde");
        }));

        assertEquals(4, intentos.get());   // 1 intento + 3 reintentos
        assertEquals(List.of(Duration.ofSeconds(1), Duration.ofSeconds(2), Duration.ofSeconds(4)), esperas);
    }

    @Test
    void unMensajeInvalidoVaDirectoALaDlqSinReintentos() {
        assertThrows(AmqpRejectAndDontRequeueException.class, () -> reintentos.ejecutar("prueba", () -> {
            intentos.incrementAndGet();
            throw new MensajeInvalidoException("Falta el campo obligatorio datos.comprador.correo");
        }));

        assertEquals(1, intentos.get());
        assertTrue(esperas.isEmpty());
    }
}
