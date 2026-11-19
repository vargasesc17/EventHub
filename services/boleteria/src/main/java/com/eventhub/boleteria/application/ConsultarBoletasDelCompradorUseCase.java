package com.eventhub.boleteria.application;

import com.eventhub.boleteria.domain.AforoEvento;
import com.eventhub.boleteria.domain.Boleta;
import com.eventhub.boleteria.domain.EstadoBoleta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * "Mis boletas": las boletas de un comprador, de la más reciente a la más antigua, con lo necesario
 * para mostrarlas sin consultar a Eventos. El nombre, la fecha y el lugar salen de la copia local del
 * evento que Boletería ya mantiene (AforoEvento), y el estado es el real: si el evento se canceló,
 * la boleta ya viene CANCELADA.
 * <p>
 * El QR solo se entrega para boletas ACTIVA: una boleta usada o cancelada no sirve para entrar, así
 * que no hay motivo para volver a exponer su código. El QR se recalcula (es determinista, ADR-0004);
 * no se guarda en la base.
 */
public class ConsultarBoletasDelCompradorUseCase {

    private final BoletaRepository boletas;
    private final AforoRepository aforos;
    private final GeneradorQr generadorQr;

    public ConsultarBoletasDelCompradorUseCase(BoletaRepository boletas, AforoRepository aforos,
                                               GeneradorQr generadorQr) {
        this.boletas = boletas;
        this.aforos = aforos;
        this.generadorQr = generadorQr;
    }

    public List<BoletaDelComprador> consultar(UUID compradorId) {
        if (compradorId == null) {
            throw new IllegalArgumentException("La consulta exige el id del comprador");
        }
        // Varias boletas del mismo evento: se busca cada evento una sola vez.
        Map<UUID, Optional<AforoEvento>> eventos = new HashMap<>();
        return boletas.buscarPorComprador(compradorId).stream()
                .map(boleta -> new BoletaDelComprador(
                        boleta,
                        eventos.computeIfAbsent(boleta.getEventoId(), aforos::buscar).orElse(null),
                        boleta.getEstado() == EstadoBoleta.ACTIVA ? generadorQr.generar(boleta.getId()) : null))
                .toList();
    }

    /** @param evento null si Boletería no conoce el evento; @param codigoQr null si la boleta no está ACTIVA */
    public record BoletaDelComprador(Boleta boleta, AforoEvento evento, String codigoQr) { }
}
