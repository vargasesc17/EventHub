package com.eventhub.boleteria.infrastructure.persistencia;

import com.eventhub.boleteria.application.BoletaRepository;
import com.eventhub.boleteria.domain.Boleta;
import com.eventhub.boleteria.domain.BoletaCortesia;
import com.eventhub.boleteria.domain.BoletaEstudiante;
import com.eventhub.boleteria.domain.BoletaVIP;
import org.springframework.stereotype.Repository;

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
        java.util.UUID emitidaPor = null;
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
}