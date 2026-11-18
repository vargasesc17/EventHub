package com.eventhub.boleteria.infrastructure.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BoletaJpaRepository extends JpaRepository<BoletaEntity, UUID> {
    long countByEventoId(UUID eventoId);
}