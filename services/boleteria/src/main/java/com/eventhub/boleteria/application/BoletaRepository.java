package com.eventhub.boleteria.application;

import com.eventhub.boleteria.domain.Boleta;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BoletaRepository {

    void guardar(Boleta boleta);

    Optional<Boleta> buscar(UUID boletaId);

    /** Las boletas de un comprador, de la compra más reciente a la más antigua ("Mis boletas"). */
    List<Boleta> buscarPorComprador(UUID compradorId);

    /**
     * Segundo paso del check-in (ADR-0004): pasa la boleta de ACTIVA a USADA con UNA actualización
     * condicional atómica. Devuelve false si no estaba ACTIVA: así, si dos personas del staff
     * escanean la misma boleta al mismo tiempo, solo una lo logra.
     */
    boolean marcarUsadaSiActiva(UUID boletaId);

    /**
     * Cuando el evento se cancela: pasa a CANCELADA todas sus boletas ACTIVA o RESERVADA,
     * con una sola actualización. Las USADA no se tocan. Devuelve cuántas cambió.
     */
    int cancelarPorEvento(UUID eventoId);
}
