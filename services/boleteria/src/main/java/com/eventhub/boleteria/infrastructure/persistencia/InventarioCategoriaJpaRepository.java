package com.eventhub.boleteria.infrastructure.persistencia;

import com.eventhub.boleteria.domain.TipoBoleta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InventarioCategoriaJpaRepository extends JpaRepository<InventarioCategoriaEntity, UUID> {

    List<InventarioCategoriaEntity> findByEventoId(UUID eventoId);

    Optional<InventarioCategoriaEntity> findByEventoIdAndCategoria(UUID eventoId, TipoBoleta categoria);

    /** ADR-0003: la condición "disponibles >= 1" la evalúa la base sobre la fila bloqueada. */
    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update InventarioCategoriaEntity i
            set i.disponibles = i.disponibles - 1, i.vendidas = i.vendidas + 1
            where i.eventoId = :eventoId and i.categoria = :categoria and i.disponibles >= 1
            """)
    int descontarCupo(@Param("eventoId") UUID eventoId, @Param("categoria") TipoBoleta categoria);
}