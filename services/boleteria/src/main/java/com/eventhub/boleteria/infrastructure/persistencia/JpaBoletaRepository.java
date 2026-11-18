package com.eventhub.boleteria.infrastructure.persistencia;

import com.eventhub.boleteria.application.BoletaRepository;
import com.eventhub.boleteria.domain.Boleta;
import com.eventhub.boleteria.domain.BoletaCortesia;
import com.eventhub.boleteria.domain.BoletaEstudiante;
import com.eventhub.boleteria.domain.BoletaGeneral;
import com.eventhub.boleteria.domain.BoletaVIP;
import com.eventhub.boleteria.domain.EstadoBoleta;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaBoletaRepository implements BoletaRepository {

    private final BoletaJpaRepository jpa;

    public JpaBoletaRepository(BoletaJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void guardar(Boleta boleta) {
        String beneficios = null;
        String carnet = null;
        UUID emitidaPor = null;
        if (boleta instanceof BoletaVIP vip) {
            beneficios = String.join("|", vip.getBeneficios());
        } else if (boleta instanceof BoletaEstudiante estudiante) {
            carnet = estudiante.getCarnetEstudiantil();
        } else if (boleta instanceof BoletaCortesia cortesia) {
            emitidaPor = cortesia.getEmitidaPor();
        }
        jpa.save(new BoletaEntity(boleta.getId(), boleta.getEventoId(), boleta.getCompradorId(), boleta.categoria(),
                boleta.getPrecioPagado(), boleta.getEstado(), boleta.getFechaCompra(), beneficios, carnet, emitidaPor));
    }

    @Override
    public Optional<Boleta> buscar(UUID boletaId) {
        return jpa.findById(boletaId).map(JpaBoletaRepository::aDominio);
    }

    @Override
    public boolean marcarUsadaSiActiva(UUID boletaId) {
        return jpa.cambiarEstadoSi(boletaId, EstadoBoleta.ACTIVA, EstadoBoleta.USADA) == 1;
    }

    @Override
    public int cancelarPorEvento(UUID eventoId) {
        return jpa.cambiarEstadoPorEvento(eventoId,
                List.of(EstadoBoleta.ACTIVA, EstadoBoleta.RESERVADA), EstadoBoleta.CANCELADA);
    }

    /** Reconstruye la subclase correcta según la categoría guardada (una tabla para los 4 tipos). */
    private static Boleta aDominio(BoletaEntity e) {
        return switch (e.getCategoria()) {
            case GENERAL -> new BoletaGeneral(e.getId(), e.getEventoId(), e.getCompradorId(), e.getPrecioPagado(),
                    e.getEstado(), e.getFechaCompra());
            case VIP -> new BoletaVIP(e.getId(), e.getEventoId(), e.getCompradorId(), e.getPrecioPagado(),
                    e.getEstado(), e.getFechaCompra(), Arrays.asList(e.getBeneficios().split("\\|")));
            case ESTUDIANTE -> new BoletaEstudiante(e.getId(), e.getEventoId(), e.getCompradorId(),
                    e.getPrecioPagado(), e.getEstado(), e.getFechaCompra(), e.getCarnetEstudiantil());
            case CORTESIA -> new BoletaCortesia(e.getId(), e.getEventoId(), e.getCompradorId(),
                    e.getPrecioPagado(), e.getEstado(), e.getFechaCompra(), e.getEmitidaPor());
        };
    }
}
