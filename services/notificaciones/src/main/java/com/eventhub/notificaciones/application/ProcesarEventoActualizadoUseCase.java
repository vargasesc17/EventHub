package com.eventhub.notificaciones.application;

import com.eventhub.notificaciones.domain.CompradorEvento;
import com.eventhub.notificaciones.domain.Correo;
import com.eventhub.notificaciones.domain.EventoLocal;
import com.eventhub.notificaciones.domain.TipoCambio;

import java.time.Clock;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * #26 · Qué hacer cuando llega un evento.actualizado (RF-04):
 * <ol>
 *   <li>Ignora duplicados (mismo messageId).</li>
 *   <li>Ignora mensajes viejos que lleguen fuera de orden (docs/contratos, "Orden").</li>
 *   <li>Actualiza la copia local del evento. El recordatorio de 24 horas se calcula con la fecha
 *       actual, así que un cambio de FECHA lo reprograma solo y con CANCELADO ya no se envía.</li>
 *   <li>Si cambia la FECHA o el LUGAR, o se CANCELA, avisa a cada comprador usando la copia local
 *       (ADR-0006), sin preguntarle a Boletería. Una persona con varias boletas recibe UN correo.</li>
 *   <li>Solo PUBLICADO o solo INVENTARIO no envían correos.</li>
 * </ol>
 */
public class ProcesarEventoActualizadoUseCase {

    public static final String TIPO = "evento.actualizado";

    private final MensajeProcesadoRepository mensajes;
    private final EventoRepository eventos;
    private final CompradorRepository compradores;
    private final EnviadorCorreo enviadorCorreo;
    private final Clock reloj;

    public ProcesarEventoActualizadoUseCase(MensajeProcesadoRepository mensajes, EventoRepository eventos,
                                            CompradorRepository compradores, EnviadorCorreo enviadorCorreo,
                                            Clock reloj) {
        this.mensajes = mensajes;
        this.eventos = eventos;
        this.compradores = compradores;
        this.enviadorCorreo = enviadorCorreo;
        this.reloj = reloj;
    }

    public Resultado ejecutar(EventoActualizadoComando comando) {
        if (mensajes.yaProcesado(comando.messageId())) {
            return Resultado.DUPLICADO;
        }
        var eventoId = comando.evento().eventoId();

        Optional<EventoLocal> actual = eventos.buscar(eventoId);
        if (actual.isPresent() && actual.get().yaAplicoUnoPosteriorA(comando.fechaEmision())) {
            registrar(comando);
            return Resultado.DESACTUALIZADO;
        }

        boolean cancelado = comando.incluye(TipoCambio.CANCELADO);
        eventos.guardar(new EventoLocal(comando.evento(), cancelado, comando.fechaEmision()));

        if (Collections.disjoint(comando.cambios(), TipoCambio.ALERTAN_A_COMPRADORES)) {
            registrar(comando);
            return Resultado.SIN_ACCION;
        }

        var destinatarios = unoPorCorreo(compradores.buscarPorEvento(eventoId));
        for (CompradorEvento comprador : destinatarios) {
            enviadorCorreo.enviar(Correo.sinQr(
                    comprador.correo(),
                    PlantillasCorreo.asuntoCambio(comando.evento(), cancelado),
                    PlantillasCorreo.cuerpoCambio(comprador.nombre(), comando.evento(), cancelado,
                            comando.fechaAnterior(), comando.lugarAnterior())));
        }
        registrar(comando);
        return destinatarios.isEmpty() ? Resultado.SIN_ACCION : Resultado.PROCESADO;
    }

    private void registrar(EventoActualizadoComando comando) {
        mensajes.registrar(comando.messageId(), TIPO, reloj.instant());
    }

    /** Si una persona compró varias boletas, recibe un solo correo (RF-04: "una sola vez"). */
    private static Collection<CompradorEvento> unoPorCorreo(Collection<CompradorEvento> todos) {
        Map<String, CompradorEvento> porCorreo = new LinkedHashMap<>();
        for (CompradorEvento c : todos) {
            porCorreo.putIfAbsent(c.correo().toLowerCase(), c);
        }
        return porCorreo.values();
    }
}
