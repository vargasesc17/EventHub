package com.eventhub.boleteria.infrastructure.persistencia;

import com.eventhub.boleteria.application.InventarioRepository;
import com.eventhub.boleteria.domain.InventarioCategoria;
import com.eventhub.boleteria.domain.ParametrosPrecio;
import com.eventhub.boleteria.domain.TipoBoleta;
import com.eventhub.boleteria.domain.TipoPrecio;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaInventarioRepository implements InventarioRepository {

    private final InventarioCategoriaJpaRepository jpa;

    public JpaInventarioRepository(InventarioCategoriaJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void crear(List<InventarioCategoria> inventarios) {
        jpa.saveAll(inventarios.stream().map(JpaInventarioRepository::aEntidad).toList());
    }

    @Override
    public List<InventarioCategoria> buscarPorEvento(UUID eventoId) {
        return jpa.findByEventoId(eventoId).stream().map(JpaInventarioRepository::aDominio).toList();
    }

    @Override
    public Optional<InventarioCategoria> buscar(UUID eventoId, TipoBoleta categoria) {
        return jpa.findByEventoIdAndCategoria(eventoId, categoria).map(JpaInventarioRepository::aDominio);
    }

    @Override
    public boolean descontarCupo(UUID eventoId, TipoBoleta categoria) {
        return jpa.descontarCupo(eventoId, categoria) == 1;
    }

    private static InventarioCategoriaEntity aEntidad(InventarioCategoria i) {
        var p = i.parametrosPrecio();
        return new InventarioCategoriaEntity(UUID.randomUUID(), i.eventoId(), i.categoria(), i.cupos(),
                i.disponibles(), i.reservadas(), i.vendidas(), i.precioBase(), i.tipoPrecio(),
                p.fechaLimite(), p.descuentoPorcentaje(), p.incrementoMaximoPorcentaje());
    }

    private static InventarioCategoria aDominio(InventarioCategoriaEntity e) {
        // Las filas creadas antes de este cambio no tienen tipo de precio: se toman como FIJO.
        var tipo = e.getTipoPrecio() == null ? TipoPrecio.FIJO : e.getTipoPrecio();
        return new InventarioCategoria(e.getEventoId(), e.getCategoria(), e.getCupos(), e.getDisponibles(),
                e.getReservadas(), e.getVendidas(), e.getPrecioBase(), tipo,
                new ParametrosPrecio(e.getFechaLimite(), e.getDescuentoPorcentaje(),
                        e.getIncrementoMaximoPorcentaje()));
    }
}