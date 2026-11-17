package com.eventhub.eventos.infrastructure.messaging;

import com.eventhub.eventos.domain.EstadoEvento;
import com.eventhub.eventos.domain.Evento;
import com.eventhub.eventos.domain.TipoBoleta;
import com.eventhub.eventos.domain.TipoCambio;
import com.eventhub.eventos.domain.ValoresAnteriores;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** El mensaje evento.actualizado tal como lo define docs/contratos (sobre común + datos). */
public record EventoActualizadoMensaje(UUID messageId, String tipo, int version, Instant fechaEmision,
                                       String origen, Datos datos) {

    public static final String TIPO = "evento.actualizado";

    public record Datos(UUID eventoId, List<TipoCambio> cambios, EstadoEvento estado, String nombre,
                        Instant fechaInicio, String lugar, Map<String, Object> valoresAnteriores,
                        int aforoMaximo, Organizador organizador, List<Categoria> categorias) { }

    public record Organizador(UUID organizadorId, String correo) { }

    public record Categoria(TipoBoleta categoria, int cupos, long precioBase) { }

    public static EventoActualizadoMensaje desde(Evento evento, Set<TipoCambio> cambios,
                                                 ValoresAnteriores anteriores) {
        var categorias = evento.getCategorias().stream()
                .map(c -> new Categoria(c.tipo(), c.cupos(), c.precioBase()))
                .toList();
        var datos = new Datos(evento.getId(), cambios.stream().sorted().toList(), evento.getEstado(),
                evento.getNombre(), evento.getFechaInicio(), evento.getLugar(),
                valoresAnteriores(anteriores), evento.getAforoMaximo(),
                new Organizador(evento.getOrganizadorId(), evento.getOrganizadorCorreo()), categorias);
        return new EventoActualizadoMensaje(UUID.randomUUID(), TIPO, 1,
                Instant.now().truncatedTo(ChronoUnit.SECONDS), "eventos", datos);
    }

    /** El contrato pide solo los campos que cambiaron, o null si no cambió ni fecha ni lugar. */
    private static Map<String, Object> valoresAnteriores(ValoresAnteriores anteriores) {
        if (anteriores == null) {
            return null;
        }
        var mapa = new LinkedHashMap<String, Object>();
        if (anteriores.fechaInicio() != null) {
            mapa.put("fechaInicio", anteriores.fechaInicio());
        }
        if (anteriores.lugar() != null) {
            mapa.put("lugar", anteriores.lugar());
        }
        return mapa.isEmpty() ? null : mapa;
    }
}