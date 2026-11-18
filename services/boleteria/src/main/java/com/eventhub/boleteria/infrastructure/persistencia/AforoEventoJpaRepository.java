package com.eventhub.boleteria.infrastructure.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

public interface AforoEventoJpaRepository extends JpaRepository<AforoEventoEntity, UUID> {

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update AforoEventoEntity a set a.fechaInicio = :fecha, a.lugar = :lugar where a.eventoId = :id")
    int actualizarProgramacion(@Param("id") UUID id, @Param("fecha") Instant fecha, @Param("lugar") String lugar);

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update AforoEventoEntity a set a.ventasBloqueadas = true where a.eventoId = :id")
    int bloquearVentas(@Param("id") UUID id);

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update AforoEventoEntity a set a.vendidas = a.vendidas + 1 where a.eventoId = :id")
    int sumarVenta(@Param("id") UUID id);

    /** ADR-0003: solo una compra logra pasar alertaEnviada de false a true. */
    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update AforoEventoEntity a set a.alertaEnviada = true
            where a.eventoId = :id and a.alertaEnviada = false
              and a.vendidas * 100 >= a.aforoMaximo * :umbral
            """)
    int marcarAlerta(@Param("id") UUID id, @Param("umbral") int umbral);
}