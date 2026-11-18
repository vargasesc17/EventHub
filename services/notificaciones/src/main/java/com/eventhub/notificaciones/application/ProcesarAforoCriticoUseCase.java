package com.eventhub.notificaciones.application;

import com.eventhub.notificaciones.domain.Correo;

import java.time.Clock;

/**
 * #26 · Qué hacer cuando llega un evento.aforo.critico: avisar al organizador (RF-05).
 * Una sola vez por evento, aunque el mensaje llegue repetido.
 */
public class ProcesarAforoCriticoUseCase {

    public static final String TIPO = "evento.aforo.critico";

    private final MensajeProcesadoRepository mensajes;
    private final AlertaAforoRepository alertas;
    private final EnviadorCorreo enviadorCorreo;
    private final Clock reloj;

    public ProcesarAforoCriticoUseCase(MensajeProcesadoRepository mensajes, AlertaAforoRepository alertas,
                                       EnviadorCorreo enviadorCorreo, Clock reloj) {
        this.mensajes = mensajes;
        this.alertas = alertas;
        this.enviadorCorreo = enviadorCorreo;
        this.reloj = reloj;
    }

    public Resultado ejecutar(AforoCriticoComando comando) {
        if (mensajes.yaProcesado(comando.messageId())) {
            return Resultado.DUPLICADO;
        }
        Resultado resultado = Resultado.SIN_ACCION;
        if (alertas.registrarSiEsPrimera(comando.eventoId(), reloj.instant())) {
            enviadorCorreo.enviar(Correo.sinQr(
                    comando.correoOrganizador(),
                    PlantillasCorreo.asuntoAforo(comando.nombreEvento()),
                    PlantillasCorreo.cuerpoAforo(comando.nombreEvento(), comando.vendidas(),
                            comando.aforoMaximo(), comando.porcentajeVendido(), comando.umbral())));
            resultado = Resultado.PROCESADO;
        }
        mensajes.registrar(comando.messageId(), TIPO, reloj.instant());
        return resultado;
    }
}
