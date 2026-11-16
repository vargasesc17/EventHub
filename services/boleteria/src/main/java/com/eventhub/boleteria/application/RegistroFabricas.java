package com.eventhub.boleteria.application;

import com.eventhub.boleteria.domain.FabricaBoleta;
import com.eventhub.boleteria.domain.ReglaDeNegocioException;
import com.eventhub.boleteria.domain.TipoBoleta;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

/** Encuentra la fábrica de cada tipo. La compra depende de esta clase, no de las fábricas concretas. */
public class RegistroFabricas {

    private final Map<TipoBoleta, FabricaBoleta> fabricas = new EnumMap<>(TipoBoleta.class);

    public RegistroFabricas(Collection<FabricaBoleta> fabricas) {
        for (var fabrica : fabricas) {
            if (this.fabricas.put(fabrica.tipo(), fabrica) != null) {
                throw new IllegalArgumentException("Hay dos fábricas para el tipo " + fabrica.tipo());
            }
        }
    }

    public FabricaBoleta fabricaPara(TipoBoleta tipo) {
        var fabrica = fabricas.get(tipo);
        if (fabrica == null) {
            throw new ReglaDeNegocioException("No hay una fábrica registrada para el tipo " + tipo);
        }
        return fabrica;
    }
}