package com.eventhub.notificaciones.infrastructure.persistencia;

import com.eventhub.notificaciones.application.RecordatorioRepository;
import com.eventhub.notificaciones.domain.InfoEvento;
import com.eventhub.notificaciones.domain.Recordatorio;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
public class JpaRecordatorioRepository implements RecordatorioRepository {

    private final EventoLocalJpaRepository eventos;
    private final CompradorEventoJpaRepository compradores;
    private final RecordatorioEnviadoJpaRepository enviados;

    public JpaRecordatorioRepository(EventoLocalJpaRepository eventos, CompradorEventoJpaRepository compradores,
                                     RecordatorioEnviadoJpaRepository enviados) {
        this.eventos = eventos;
        this.compradores = compradores;
        this.enviados = enviados;
    }

    @Override
    public List<Recordatorio> pendientes(Instant desde, Instant hasta) {
        List<Recordatorio> pendientes = new ArrayList<>();
        for (var evento : eventos.findByCanceladoFalseAndFechaInicioBetween(desde, hasta)) {
            var info = new InfoEvento(evento.getEventoId(), evento.getNombre(), evento.getFechaInicio(),
                    evento.getLugar());
            for (var comprador : compradores.findByEventoIdOrderByRegistradoEn(evento.getEventoId())) {
                String id = RecordatorioEnviadoEntity.idDe(comprador.getBoletaId(), evento.getFechaInicio());
                if (!enviados.existsById(id)) {
                    pendientes.add(new Recordatorio(comprador.getBoletaId(), comprador.getNombre(),
                            comprador.getCorreo(), info));
                }
            }
        }
        return pendientes;
    }

    @Override
    public void marcarEnviado(UUID boletaId, Instant fechaInicioEvento, Instant enviadoEn) {
        enviados.save(new RecordatorioEnviadoEntity(boletaId, fechaInicioEvento, enviadoEn));
    }
}
