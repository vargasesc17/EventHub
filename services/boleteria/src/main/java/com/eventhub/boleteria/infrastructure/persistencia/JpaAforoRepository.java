package com.eventhub.boleteria.infrastructure.persistencia;

import com.eventhub.boleteria.application.AforoRepository;
import com.eventhub.boleteria.domain.AforoEvento;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaAforoRepository implements AforoRepository {

    private final AforoEventoJpaRepository jpa;

    public JpaAforoRepository(AforoEventoJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public boolean existe(UUID eventoId) {
        return jpa.existsById(eventoId);
    }

    @Override
    public void crear(AforoEvento a) {
        jpa.save(new AforoEventoEntity(a.eventoId(), a.nombre(), a.fechaInicio(), a.lugar(), a.organizadorId(),
                a.organizadorCorreo(), a.aforoMaximo(), a.vendidas(), a.alertaEnviada(), a.ventasBloqueadas()));
    }

    @Override
    public Optional<AforoEvento> buscar(UUID eventoId) {
        return jpa.findById(eventoId).map(e -> new AforoEvento(e.getEventoId(), e.getNombre(), e.getFechaInicio(),
                e.getLugar(), e.getOrganizadorId(), e.getOrganizadorCorreo(), e.getAforoMaximo(), e.getVendidas(),
                e.isAlertaEnviada(), e.isVentasBloqueadas()));
    }

    @Override
    public void actualizarProgramacion(UUID eventoId, Instant fechaInicio, String lugar) {
        jpa.actualizarProgramacion(eventoId, fechaInicio, lugar);
    }

    @Override
    public void bloquearVentas(UUID eventoId) {
        jpa.bloquearVentas(eventoId);
    }
}