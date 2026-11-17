package com.eventhub.boleteria.application;

import com.eventhub.boleteria.domain.AforoEvento;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

class AforosEnMemoria implements AforoRepository {
    private final Map<UUID, AforoEvento> aforos = new HashMap<>();

    @Override
    public boolean existe(UUID eventoId) { return aforos.containsKey(eventoId); }

    @Override
    public void crear(AforoEvento aforo) { aforos.put(aforo.eventoId(), aforo); }

    @Override
    public Optional<AforoEvento> buscar(UUID eventoId) { return Optional.ofNullable(aforos.get(eventoId)); }

    @Override
    public void actualizarProgramacion(UUID eventoId, Instant fechaInicio, String lugar) {
        var a = aforos.get(eventoId);
        guardar(a, fechaInicio, lugar, a.vendidas(), a.alertaEnviada(), a.ventasBloqueadas());
    }

    @Override
    public void bloquearVentas(UUID eventoId) {
        var a = aforos.get(eventoId);
        guardar(a, a.fechaInicio(), a.lugar(), a.vendidas(), a.alertaEnviada(), true);
    }

    @Override
    public void sumarVenta(UUID eventoId) {
        var a = aforos.get(eventoId);
        guardar(a, a.fechaInicio(), a.lugar(), a.vendidas() + 1, a.alertaEnviada(), a.ventasBloqueadas());
    }

    @Override
    public boolean marcarAlertaSiAlcanzaUmbral(UUID eventoId, int umbralPorcentaje) {
        var a = aforos.get(eventoId);
        if (a.alertaEnviada() || !a.alcanzaUmbral(umbralPorcentaje)) {
            return false;
        }
        guardar(a, a.fechaInicio(), a.lugar(), a.vendidas(), true, a.ventasBloqueadas());
        return true;
    }

    private void guardar(AforoEvento a, Instant fecha, String lugar, int vendidas, boolean alerta, boolean bloqueadas) {
        aforos.put(a.eventoId(), new AforoEvento(a.eventoId(), a.nombre(), fecha, lugar, a.organizadorId(),
                a.organizadorCorreo(), a.aforoMaximo(), vendidas, alerta, bloqueadas));
    }
}