package com.eventhub.boleteria.infrastructure.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CheckInJpaRepository extends JpaRepository<CheckInEntity, UUID> {
    List<CheckInEntity> findByBoletaId(UUID boletaId);
}
