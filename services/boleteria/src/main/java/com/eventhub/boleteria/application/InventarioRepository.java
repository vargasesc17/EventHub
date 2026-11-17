package com.eventhub.boleteria.application;

import com.eventhub.boleteria.domain.InventarioCategoria;

import java.util.List;
import java.util.UUID;

/** Inventario por categoría (ADR-0003). El descuento atómico de la compra se agrega con el caso de uso de compra. */
public interface InventarioRepository {

    void crear(List<InventarioCategoria> inventarios);

    List<InventarioCategoria> buscarPorEvento(UUID eventoId);
}