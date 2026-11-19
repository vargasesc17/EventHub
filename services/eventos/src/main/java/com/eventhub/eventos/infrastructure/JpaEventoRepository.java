package com.eventhub.eventos.infrastructure;

import com.eventhub.eventos.application.EventoRepository;
import com.eventhub.eventos.domain.EstadoEvento;
import com.eventhub.eventos.domain.Evento;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaEventoRepository implements EventoRepository {

    private final EventoJpaRepository jpa;

    public JpaEventoRepository(EventoJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void guardar(Evento evento) {
        jpa.save(EventoMapper.aEntidad(evento));
    }

    @Override
    public Optional<Evento> buscarPorId(UUID id) {
        return jpa.findById(id).map(EventoMapper::aDominio);
    }

    @Override
    public List<Evento> buscarPublicadosDesde(Instant ahora) {
        return jpa.findByEstadoAndFechaInicioAfter(EstadoEvento.PUBLICADO, ahora).stream()
                .map(EventoMapper::aDominio)
                .toList();
    }

    @Override
    public List<Evento> buscarPorOrganizador(UUID organizadorId) {
        return jpa.findByOrganizadorIdOrderByFechaInicioAsc(organizadorId).stream()
                .map(EventoMapper::aDominio)
                .toList();
    }
}
