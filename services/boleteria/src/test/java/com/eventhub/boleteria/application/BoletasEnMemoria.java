package com.eventhub.boleteria.application;

import com.eventhub.boleteria.domain.Boleta;

import java.util.ArrayList;
import java.util.List;

class BoletasEnMemoria implements BoletaRepository {
    private final List<Boleta> guardadas = new ArrayList<>();

    @Override
    public void guardar(Boleta boleta) { guardadas.add(boleta); }

    List<Boleta> guardadas() { return guardadas; }
}