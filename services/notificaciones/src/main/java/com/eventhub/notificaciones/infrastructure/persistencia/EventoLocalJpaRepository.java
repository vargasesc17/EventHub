package com.eventhub.notificaciones.infrastructure.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EventoLocalJpaRepository extends JpaRepository<EventoLocalEntity, UUID> {
}
