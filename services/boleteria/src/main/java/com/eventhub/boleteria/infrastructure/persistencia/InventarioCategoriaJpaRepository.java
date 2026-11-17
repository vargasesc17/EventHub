package com.eventhub.boleteria.infrastructure.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InventarioCategoriaJpaRepository extends JpaRepository<InventarioCategoriaEntity, UUID> {
    List<InventarioCategoriaEntity> findByEventoId(UUID eventoId);
}