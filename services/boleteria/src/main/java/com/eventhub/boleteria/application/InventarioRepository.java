package com.eventhub.boleteria.application;

import com.eventhub.boleteria.domain.InventarioCategoria;
import com.eventhub.boleteria.domain.TipoBoleta;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Inventario por categoría (ADR-0003). El descuento atómico de la compra se agrega con el caso de uso de compra. */
public interface InventarioRepository {

    void crear(List<InventarioCategoria> inventarios);

    List<InventarioCategoria> buscarPorEvento(UUID eventoId);

    Optional<InventarioCategoria> buscar(UUID eventoId, TipoBoleta categoria);

    /** Descuenta un cupo de forma atómica (ADR-0003). Devuelve false si no quedaba. */
    boolean descontarCupo(UUID eventoId, TipoBoleta categoria);
}