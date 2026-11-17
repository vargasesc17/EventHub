package com.eventhub.boleteria.application;

import com.eventhub.boleteria.domain.AforoEvento;
import com.eventhub.boleteria.domain.InventarioCategoria;

import java.util.List;
import java.util.UUID;

/** RF-05: el organizador consulta cuántas boletas se han vendido frente al aforo. */
public class ConsultarAforoUseCase {

    private final AforoRepository aforos;
    private final InventarioRepository inventarios;

    public ConsultarAforoUseCase(AforoRepository aforos, InventarioRepository inventarios) {
        this.aforos = aforos;
        this.inventarios = inventarios;
    }

    public AforoConsultado consultar(UUID eventoId) {
        var evento = aforos.buscar(eventoId).orElseThrow(() -> new EventoNoEncontradoException(eventoId));
        return new AforoConsultado(evento, inventarios.buscarPorEvento(eventoId));
    }

    public record AforoConsultado(AforoEvento evento, List<InventarioCategoria> categorias) { }
}