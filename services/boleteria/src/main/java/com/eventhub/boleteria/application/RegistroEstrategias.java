package com.eventhub.boleteria.application;

import com.eventhub.boleteria.domain.PrecioStrategy;
import com.eventhub.boleteria.domain.ReglaDeNegocioException;
import com.eventhub.boleteria.domain.TipoPrecio;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

/** Encuentra la estrategia de cada tipo de precio. La compra depende de esta clase y de PrecioStrategy. */
public class RegistroEstrategias {

    private final Map<TipoPrecio, PrecioStrategy> estrategias = new EnumMap<>(TipoPrecio.class);

    public RegistroEstrategias(Collection<PrecioStrategy> estrategias) {
        for (var estrategia : estrategias) {
            if (this.estrategias.put(estrategia.tipo(), estrategia) != null) {
                throw new IllegalArgumentException("Hay dos estrategias para el tipo " + estrategia.tipo());
            }
        }
    }

    public PrecioStrategy estrategiaPara(TipoPrecio tipo) {
        var estrategia = estrategias.get(tipo);
        if (estrategia == null) {
            throw new ReglaDeNegocioException("No hay una estrategia registrada para el tipo " + tipo);
        }
        return estrategia;
    }
}