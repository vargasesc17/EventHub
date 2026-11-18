package com.eventhub.notificaciones.infrastructure.persistencia;

import com.eventhub.notificaciones.application.AlertaAforoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.UUID;

@Repository
public class JpaAlertaAforoRepository implements AlertaAforoRepository {

    private final AlertaAforoJpaRepository jpa;

    public JpaAlertaAforoRepository(AlertaAforoJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public boolean registrarSiEsPrimera(UUID eventoId, Instant enviadaEn) {
        if (jpa.existsById(eventoId)) {
            return false;
        }
        jpa.save(new AlertaAforoEntity(eventoId, enviadaEn));
        return true;
    }
}
