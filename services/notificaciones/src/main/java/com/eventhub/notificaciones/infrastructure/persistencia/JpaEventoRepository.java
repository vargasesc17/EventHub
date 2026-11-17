package com.eventhub.notificaciones.infrastructure.persistencia;

import com.eventhub.notificaciones.application.EventoRepository;
import com.eventhub.notificaciones.domain.EventoLocal;
import com.eventhub.notificaciones.domain.InfoEvento;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaEventoRepository implements EventoRepository {

    private final EventoLocalJpaRepository jpa;

    public JpaEventoRepository(EventoLocalJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<EventoLocal> buscar(UUID eventoId) {
        return jpa.findById(eventoId).map(e -> new EventoLocal(
                new InfoEvento(e.getEventoId(), e.getNombre(), e.getFechaInicio(), e.getLugar()),
                e.isCancelado(), e.getUltimaFechaEmision()));
    }

    @Override
    public void guardar(EventoLocal evento) {
        var info = evento.info();
        jpa.save(new EventoLocalEntity(info.eventoId(), info.nombre(), info.fechaInicio(), info.lugar(),
                evento.cancelado(), evento.ultimaFechaEmision()));
    }
}
