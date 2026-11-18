package com.eventhub.boleteria.application;

import com.eventhub.boleteria.domain.InventarioCategoria;
import com.eventhub.boleteria.domain.TipoBoleta;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

class InventariosEnMemoria implements InventarioRepository {
    private final List<InventarioCategoria> inventarios = new ArrayList<>();

    @Override
    public void crear(List<InventarioCategoria> nuevos) { inventarios.addAll(nuevos); }

    @Override
    public List<InventarioCategoria> buscarPorEvento(UUID eventoId) {
        return inventarios.stream().filter(i -> i.eventoId().equals(eventoId)).toList();
    }

    @Override
    public Optional<InventarioCategoria> buscar(UUID eventoId, TipoBoleta categoria) {
        return inventarios.stream()
                .filter(i -> i.eventoId().equals(eventoId) && i.categoria() == categoria)
                .findFirst();
    }

    @Override
    public boolean descontarCupo(UUID eventoId, TipoBoleta categoria) {
        for (int k = 0; k < inventarios.size(); k++) {
            var i = inventarios.get(k);
            if (i.eventoId().equals(eventoId) && i.categoria() == categoria) {
                if (i.disponibles() < 1) {
                    return false;
                }
                inventarios.set(k, new InventarioCategoria(i.eventoId(), i.categoria(), i.cupos(),
                        i.disponibles() - 1, i.reservadas(), i.vendidas() + 1, i.precioBase(),
                        i.tipoPrecio(), i.parametrosPrecio()));
                return true;
            }
        }
        return false;
    }
}