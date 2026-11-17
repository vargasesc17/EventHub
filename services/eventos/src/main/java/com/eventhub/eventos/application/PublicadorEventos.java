package com.eventhub.eventos.application;

import com.eventhub.eventos.domain.Evento;
import com.eventhub.eventos.domain.TipoCambio;
import com.eventhub.eventos.domain.ValoresAnteriores;

import java.util.Set;

public interface PublicadorEventos {
    /**
     * Avisa que un evento cambió (contrato evento.actualizado).
     * @param anteriores null cuando no cambió ni la fecha ni el lugar
     */
    void eventoActualizado(Evento evento, Set<TipoCambio> cambios, ValoresAnteriores anteriores);
}