package com.eventhub.notificaciones.infrastructure.persistencia;

import com.eventhub.notificaciones.application.CompradorRepository;
import com.eventhub.notificaciones.domain.CompradorEvento;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class JpaCompradorRepository implements CompradorRepository {

    private final CompradorEventoJpaRepository jpa;

    public JpaCompradorRepository(CompradorEventoJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void guardarSiNoExiste(CompradorEvento c) {
        if (!jpa.existsById(c.boletaId())) {
            jpa.save(new CompradorEventoEntity(c.boletaId(), c.eventoId(), c.compradorId(),
                    c.nombre(), c.correo(), c.registradoEn()));
        }
    }

    @Override
    public List<CompradorEvento> buscarPorEvento(UUID eventoId) {
        return jpa.findByEventoIdOrderByRegistradoEn(eventoId).stream()
                .map(e -> new CompradorEvento(e.getBoletaId(), e.getEventoId(), e.getCompradorId(),
                        e.getNombre(), e.getCorreo(), e.getRegistradoEn()))
                .toList();
    }
}
