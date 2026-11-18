package com.eventhub.notificaciones.application;

import com.eventhub.notificaciones.domain.CompradorEvento;
import com.eventhub.notificaciones.domain.Correo;
import com.eventhub.notificaciones.domain.EventoLocal;
import com.eventhub.notificaciones.domain.Recordatorio;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Implementaciones falsas de los puertos, en memoria. Con ellas los casos de uso se prueban sin
 * RabbitMQ, base de datos ni servidor de correo: es la ventaja de depender de interfaces (DIP).
 */
final class PuertosEnMemoria {

    private PuertosEnMemoria() { }

    static class Mensajes implements MensajeProcesadoRepository {
        final Set<UUID> procesados = new HashSet<>();
        public boolean yaProcesado(UUID messageId) { return procesados.contains(messageId); }
        public void registrar(UUID messageId, String tipo, Instant procesadoEn) { procesados.add(messageId); }
    }

    static class Eventos implements EventoRepository {
        final Map<UUID, EventoLocal> eventos = new HashMap<>();
        public Optional<EventoLocal> buscar(UUID eventoId) { return Optional.ofNullable(eventos.get(eventoId)); }
        public void guardar(EventoLocal evento) { eventos.put(evento.info().eventoId(), evento); }
    }

    static class Compradores implements CompradorRepository {
        final Map<UUID, CompradorEvento> porBoleta = new LinkedHashMap<>();
        public void guardarSiNoExiste(CompradorEvento c) { porBoleta.putIfAbsent(c.boletaId(), c); }
        public List<CompradorEvento> buscarPorEvento(UUID eventoId) {
            return porBoleta.values().stream().filter(c -> c.eventoId().equals(eventoId)).toList();
        }
    }

    static class Correos implements EnviadorCorreo {
        final List<Correo> enviados = new ArrayList<>();
        boolean fallar;
        public void enviar(Correo correo) {
            if (fallar) {
                throw new IllegalStateException("El servidor de correo no responde");
            }
            enviados.add(correo);
        }
        List<String> destinatarios() { return enviados.stream().map(Correo::para).toList(); }
    }

    static class Alertas implements AlertaAforoRepository {
        final Set<UUID> eventos = new HashSet<>();
        public boolean registrarSiEsPrimera(UUID eventoId, Instant enviadaEn) { return eventos.add(eventoId); }
    }

    /** Usa los eventos y compradores en memoria, igual que el adaptador JPA usa las tablas. */
    static class Recordatorios implements RecordatorioRepository {
        final Eventos eventos;
        final Compradores compradores;
        final Set<String> enviados = new HashSet<>();

        Recordatorios(Eventos eventos, Compradores compradores) {
            this.eventos = eventos;
            this.compradores = compradores;
        }

        public List<Recordatorio> pendientes(Instant desde, Instant hasta) {
            List<Recordatorio> pendientes = new ArrayList<>();
            for (EventoLocal e : eventos.eventos.values()) {
                var inicio = e.info().fechaInicio();
                if (e.cancelado() || inicio.isBefore(desde) || inicio.isAfter(hasta)) {
                    continue;
                }
                for (CompradorEvento c : compradores.buscarPorEvento(e.info().eventoId())) {
                    if (!enviados.contains(c.boletaId() + "@" + inicio)) {
                        pendientes.add(new Recordatorio(c.boletaId(), c.nombre(), c.correo(), e.info()));
                    }
                }
            }
            return pendientes;
        }

        public void marcarEnviado(UUID boletaId, Instant fechaInicioEvento, Instant enviadoEn) {
            enviados.add(boletaId + "@" + fechaInicioEvento);
        }
    }
}
