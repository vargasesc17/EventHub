package com.eventhub.boleteria.infrastructure.persistencia;

import com.eventhub.boleteria.application.MensajeProcesadoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.UUID;

@Repository
public class JpaMensajeProcesadoRepository implements MensajeProcesadoRepository {

    private final MensajeProcesadoJpaRepository jpa;

    public JpaMensajeProcesadoRepository(MensajeProcesadoJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public boolean yaProcesado(UUID messageId) {
        return jpa.existsById(messageId);
    }

    @Override
    public void registrar(UUID messageId, String tipo, Instant procesadoEn) {
        jpa.save(new MensajeProcesadoEntity(messageId, tipo, procesadoEn));
    }
}