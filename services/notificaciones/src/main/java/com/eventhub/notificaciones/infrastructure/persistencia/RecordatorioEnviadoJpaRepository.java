package com.eventhub.notificaciones.infrastructure.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RecordatorioEnviadoJpaRepository extends JpaRepository<RecordatorioEnviadoEntity, String> {
}
