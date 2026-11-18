package com.eventhub.boleteria.application;

import com.eventhub.boleteria.domain.AforoEvento;
import com.eventhub.boleteria.domain.InventarioCategoria;

import java.time.Clock;

/**
 * Qué hace Boletería cuando llega un evento.actualizado:
 * PUBLICADO crea la copia del evento y su inventario; FECHA y LUGAR actualizan la copia;
 * CANCELADO bloquea las ventas y cancela las boletas vendidas. INVENTARIO todavía no se aplica (Eventos no lo publica).
 */
public class SincronizarEventoUseCase {

    public static final String TIPO = "evento.actualizado";

    private final MensajeProcesadoRepository mensajes;
    private final AforoRepository aforos;
    private final InventarioRepository inventarios;
    private final Clock reloj;
    private final BoletaRepository boletas;

    public SincronizarEventoUseCase(MensajeProcesadoRepository mensajes, AforoRepository aforos,
                                    InventarioRepository inventarios, BoletaRepository boletas, Clock reloj) {
        this.mensajes = mensajes;
        this.aforos = aforos;
        this.inventarios = inventarios;
        this.boletas = boletas;
        this.reloj = reloj;
    }
    public Resultado ejecutar(EventoActualizadoComando comando) {
        if (mensajes.yaProcesado(comando.messageId())) {
            return Resultado.DUPLICADO;
        }
        Resultado resultado = aplicar(comando);
        mensajes.registrar(comando.messageId(), TIPO, reloj.instant());
        return resultado;
    }

    private Resultado aplicar(EventoActualizadoComando comando) {
        var eventoId = comando.eventoId();
        var cambios = comando.cambios();
        boolean conocido = aforos.existe(eventoId);

        if (cambios.contains(TipoCambio.CANCELADO)) {
            if (!conocido) {
                return Resultado.IGNORADO;   // nunca se vendió aquí: no hay nada que bloquear
            }
            aforos.bloquearVentas(eventoId);
            aforos.bloquearVentas(eventoId);
            boletas.cancelarPorEvento(eventoId);   // y el check-in las rechaza ("La boleta está cancelada")
            return Resultado.PROCESADO;
        }
        if (!conocido) {
            // Cada mensaje trae el estado completo del evento: sirve para crearlo aunque
            // el PUBLICADO original no haya llegado.
            crear(comando);
            return Resultado.PROCESADO;
        }
        if (cambios.contains(TipoCambio.FECHA) || cambios.contains(TipoCambio.LUGAR)) {
            aforos.actualizarProgramacion(eventoId, comando.fechaInicio(), comando.lugar());
            return Resultado.PROCESADO;
        }
        return Resultado.IGNORADO;   // un PUBLICADO repetido, o INVENTARIO
    }

    private void crear(EventoActualizadoComando comando) {
        aforos.crear(AforoEvento.nuevo(comando.eventoId(), comando.nombre(), comando.fechaInicio(),
                comando.lugar(), comando.organizadorId(), comando.organizadorCorreo(), comando.aforoMaximo()));
        inventarios.crear(comando.categorias().stream()
                .map(c -> InventarioCategoria.nuevo(comando.eventoId(), c.categoria(), c.cupos(), c.precioBase()))
                .toList());
    }
}