package com.eventhub.boleteria.infrastructure.persistencia;

import com.eventhub.boleteria.application.InventarioRepository;
import com.eventhub.boleteria.domain.InventarioCategoria;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class JpaInventarioRepository implements InventarioRepository {

    private final InventarioCategoriaJpaRepository jpa;

    public JpaInventarioRepository(InventarioCategoriaJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void crear(List<InventarioCategoria> inventarios) {
        jpa.saveAll(inventarios.stream()
                .map(i -> new InventarioCategoriaEntity(UUID.randomUUID(), i.eventoId(), i.categoria(), i.cupos(),
                        i.disponibles(), i.reservadas(), i.vendidas(), i.precioBase()))
                .toList());
    }

    @Override
    public List<InventarioCategoria> buscarPorEvento(UUID eventoId) {
        return jpa.findByEventoId(eventoId).stream()
                .map(e -> new InventarioCategoria(e.getEventoId(), e.getCategoria(), e.getCupos(), e.getDisponibles(),
                        e.getReservadas(), e.getVendidas(), e.getPrecioBase()))
                .toList();
    }
}