package com.eventhub.boleteria.infrastructure.persistencia;

import com.eventhub.boleteria.application.CheckInRepository;
import com.eventhub.boleteria.domain.CheckIn;
import org.springframework.stereotype.Repository;

@Repository
public class JpaCheckInRepository implements CheckInRepository {

    private final CheckInJpaRepository jpa;

    public JpaCheckInRepository(CheckInJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void registrar(CheckIn c) {
        jpa.save(new CheckInEntity(c.id(), c.boletaId(), c.eventoId(), c.staffId(), c.fecha()));
    }
}
