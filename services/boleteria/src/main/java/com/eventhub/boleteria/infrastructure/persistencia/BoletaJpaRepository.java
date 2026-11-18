package com.eventhub.boleteria.infrastructure.persistencia;

import com.eventhub.boleteria.domain.EstadoBoleta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public interface BoletaJpaRepository extends JpaRepository<BoletaEntity, UUID> {
    long countByEventoId(UUID eventoId);

    /**
     * ADR-0004: UPDATE boleta SET estado = 'USADA' WHERE id = :id AND estado = 'ACTIVA'.
     * La base evalúa la condición sobre la fila bloqueada: si dos escaneos llegan juntos, solo uno cambia la fila.
     */
    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update BoletaEntity b set b.estado = :nuevo where b.id = :id and b.estado = :actual")
    int cambiarEstadoSi(@Param("id") UUID id, @Param("actual") EstadoBoleta actual, @Param("nuevo") EstadoBoleta nuevo);
}
