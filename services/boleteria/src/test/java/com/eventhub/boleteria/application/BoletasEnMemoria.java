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

    @Override
    public void guardar(Boleta boleta) { guardadas.add(boleta); }

    @Override
    public Optional<Boleta> buscar(UUID boletaId) {
        consultas++;
        return guardadas.stream().filter(b -> b.getId().equals(boletaId)).findFirst();
    }

    /** Igual que el UPDATE condicional: solo la primera vez, y solo si estaba ACTIVA. */
    @Override
    public boolean marcarUsadaSiActiva(UUID boletaId) {
        var boleta = buscar(boletaId);
        consultas--;   // no cuenta como consulta del caso de uso
        return boleta.isPresent() && boleta.get().puedeIngresar() && usadas.add(boletaId);
    }

    List<Boleta> guardadas() { return guardadas; }

    /** Cuántas veces el caso de uso fue a buscar una boleta "a la base". */
    int consultas() { return consultas; }
}
