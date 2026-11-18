package com.eventhub.boleteria.application;

import com.eventhub.boleteria.domain.Boleta;

public interface BoletaRepository {
    void guardar(Boleta boleta);
}