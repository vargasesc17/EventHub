package com.eventhub.boleteria.application;

import com.eventhub.boleteria.domain.Boleta;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

class BoletasEnMemoria implements BoletaRepository {
    private final List<Boleta> guardadas = new ArrayList<>();
    private final Set<UUID> usadas = new HashSet<>();
    private int consultas;
    private final Set<UUID> canceladas = new HashSet<>();

    @Override
    public int cancelarPorEvento(UUID eventoId) {
        int cambiadas = 0;
        for (var boleta : guardadas) {
            if (boleta.getEventoId().equals(eventoId) && !usadas.contains(boleta.getId())
                    && canceladas.add(boleta.getId())) {
                cambiadas++;
            }
        }
        return cambiadas;
    }

    @Override
    public void guardar(Boleta boleta) { guardadas.add(boleta); }

    @Override
    public Optional<Boleta> buscar(UUID boletaId) {
        consultas++;
        return guardadas.stream().filter(b -> b.getId().equals(boletaId)).findFirst();
    }

    @Override
    public List<Boleta> buscarPorComprador(UUID compradorId) {
        return guardadas.stream()
                .filter(b -> b.getCompradorId().equals(compradorId))
                .sorted(java.util.Comparator.comparing(Boleta::getFechaCompra).reversed())
                .toList();
    }

    /** Igual que el UPDATE condicional: solo la primera vez, y solo si estaba ACTIVA. */
    @Override
    public boolean marcarUsadaSiActiva(UUID boletaId) {
        var boleta = buscar(boletaId);
        consultas--;   // no cuenta como consulta del caso de uso
        return boleta.isPresent() && boleta.get().puedeIngresar()
                && !canceladas.contains(boletaId) && usadas.add(boletaId);
    }

    List<Boleta> guardadas() { return guardadas; }

    /** Cuántas veces el caso de uso fue a buscar una boleta "a la base". */
    int consultas() { return consultas; }

    Set<UUID> canceladas() { return canceladas; }
}
