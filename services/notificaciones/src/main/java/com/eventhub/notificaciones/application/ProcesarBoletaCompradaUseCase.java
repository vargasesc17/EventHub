package com.eventhub.notificaciones.application;

import com.eventhub.notificaciones.domain.CompradorEvento;
import com.eventhub.notificaciones.domain.Correo;
import com.eventhub.notificaciones.domain.EventoLocal;

import java.time.Clock;

/**
 * #25 · Qué hacer cuando llega un boleta.comprada (RF-04):
 * <ol>
 *   <li>Si el mensaje ya se procesó, no hace nada (idempotencia).</li>
 *   <li>Guarda el evento si no lo conocía, y el comprador en la copia local (ADR-0006).</li>
 *   <li>Envía el correo de confirmación con el QR.</li>
 *   <li>Registra el messageId como procesado.</li>
 * </ol>
 * Solo depende de puertos (interfaces de Java): por eso se prueba sin RabbitMQ, sin base de datos
 * y sin servidor de correo (DIP).
 */
public class ProcesarBoletaCompradaUseCase {

    public static final String TIPO = "boleta.comprada";

    private final MensajeProcesadoRepository mensajes;
    private final EventoRepository eventos;
    private final CompradorRepository compradores;
    private final GeneradorImagenQr generadorQr;
    private final EnviadorCorreo enviadorCorreo;
    private final Clock reloj;

    public ProcesarBoletaCompradaUseCase(MensajeProcesadoRepository mensajes, EventoRepository eventos,
                                         CompradorRepository compradores, GeneradorImagenQr generadorQr,
                                         EnviadorCorreo enviadorCorreo, Clock reloj) {
        this.mensajes = mensajes;
        this.eventos = eventos;
        this.compradores = compradores;
        this.generadorQr = generadorQr;
        this.enviadorCorreo = enviadorCorreo;
        this.reloj = reloj;
    }

    public Resultado ejecutar(BoletaCompradaComando comando) {
        if (mensajes.yaProcesado(comando.messageId())) {
            return Resultado.DUPLICADO;
        }

        // Si ya conocemos el evento por un evento.actualizado (#26), esos datos son más recientes:
        // no los pisamos con la copia que trae la compra.
        if (eventos.buscar(comando.evento().eventoId()).isEmpty()) {
            eventos.guardar(EventoLocal.desdeCompra(comando.evento()));
        }

        compradores.guardarSiNoExiste(new CompradorEvento(
                comando.boletaId(), comando.evento().eventoId(), comando.compradorId(),
                comando.nombreComprador(), comando.correoComprador(), reloj.instant()));

        byte[] qr = generadorQr.generarPng(comando.codigoQr());
        enviadorCorreo.enviar(new Correo(
                comando.correoComprador(),
                PlantillasCorreo.asuntoConfirmacion(comando.evento()),
                PlantillasCorreo.cuerpoConfirmacion(comando.nombreComprador(), comando.evento(),
                        comando.categoria()),
                qr));

        mensajes.registrar(comando.messageId(), TIPO, reloj.instant());
        return Resultado.PROCESADO;
    }
}
